package com.aiimage.controller;

import com.aiimage.dto.ApiResponse;
import com.aiimage.entity.EmailCode;
import com.aiimage.entity.PointLog;
import com.aiimage.entity.SysUser;
import com.aiimage.service.EmailCodeService;
import com.aiimage.service.PointLogService;
import com.aiimage.service.SysUserService;
import com.aiimage.service.SystemConfigService;
import com.aiimage.util.EmailUtil;
import com.aiimage.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户认证", description = "用户登录、注册等认证相关接口")
@RequiredArgsConstructor
public class AuthController {
    
    private final SysUserService sysUserService;
    private final EmailCodeService emailCodeService;
    private final PointLogService pointLogService;
    private final SystemConfigService systemConfigService;
    private final EmailUtil emailUtil;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** Base64 解码前端传来的密码 */
    private String decodePassword(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encoded; // 兼容旧格式或非 Base64 字符串
        }
    }

    @PostMapping("/sendCode")
    @Operation(summary = "发送邮箱验证码")
    public ApiResponse<Void> sendCode(@RequestParam String email) {
        try {
            // 生成 6 位验证码
            String code = RandomStringUtils.randomNumeric(6);
            
            // 保存验证码到数据库
            EmailCode emailCode = EmailCode.builder()
                    .email(email)
                    .code(code)
                    .expireAt(LocalDateTime.now().plusMinutes(10))
                    .used(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            emailCodeService.save(emailCode);
            
            // 发送邮件
            emailUtil.sendVerificationCode(email, code);
            
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error("发送验证码失败: " + e.getMessage());
        }
    }

    @GetMapping("/checkEmail")
    @Operation(summary = "检查邮箱是否已注册")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        try {
            SysUser user = sysUserService.findByEmail(email);
            return ApiResponse.success(user != null);
        } catch (Exception e) {
            return ApiResponse.error("检查失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "邮箱密码登录")
    public ApiResponse<Map<String, Object>> login(@RequestParam String email, @RequestParam String password) {
        try {
            // 查找用户
            SysUser user = sysUserService.findByEmail(email);
            if (user == null) {
                return ApiResponse.error(400, "邮箱或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(decodePassword(password), user.getPassword())) {
                return ApiResponse.error(400, "邮箱或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != 1) {
                return ApiResponse.error(403, "账号已被禁用");
            }

            // 生成 JWT Token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<Map<String, Object>> register(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String password,
            @RequestParam(required = false) String inviteCode) {
        try {
            // 检查邮箱是否已注册
            SysUser existingUser = sysUserService.findByEmail(email);
            if (existingUser != null) {
                return ApiResponse.error(400, "该邮箱已注册");
            }

            // 验证验证码
            EmailCode emailCode = emailCodeService.getOne(
                    new QueryWrapper<EmailCode>()
                            .eq("email", email)
                            .eq("code", code)
                            .eq("used", 0)
                            .gt("expire_at", LocalDateTime.now())
                            .orderByDesc("created_at")
                            .last("LIMIT 1")
            );

            if (emailCode == null) {
                return ApiResponse.error(400, "验证码无效或已过期");
            }

            // 标记验证码为已使用
            emailCode.setUsed(1);
            emailCodeService.updateById(emailCode);

            // 创建新用户
            SysUser user = SysUser.builder()
                    .email(email)
                    .password(passwordEncoder.encode(decodePassword(password)))
                    .level(1)
                    .inviteCode(sysUserService.generateInviteCode())
                    .status(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            sysUserService.save(user);

            // 注册赠送积分
            String initPointsStr = systemConfigService.getValue("init_points");
            int initPoints = 30;
            try {
                if (initPointsStr != null && !initPointsStr.isEmpty()) initPoints = Integer.parseInt(initPointsStr);
            } catch (NumberFormatException ignored) {}

            PointLog regLog = PointLog.builder()
                    .userId(user.getId())
                    .type(1)
                    .amount(initPoints)
                    .balance(initPoints)
                    .remark("注册赠送")
                    .isSettled(1)
                    .createdAt(LocalDateTime.now())
                    .build();
            pointLogService.save(regLog);

            // 处理邀请关系
            if (inviteCode != null && !inviteCode.isBlank()) {
                SysUser inviter = sysUserService.getOne(
                        new QueryWrapper<SysUser>().eq("invite_code", inviteCode)
                );
                if (inviter != null) {
                    user.setInviteeId(inviter.getId());
                    sysUserService.updateById(user);

                    String invitePointsStr = systemConfigService.getValue("invite_points");
                    int invitePoints = 10;
                    try {
                        if (invitePointsStr != null && !invitePointsStr.isEmpty()) invitePoints = Integer.parseInt(invitePointsStr);
                    } catch (NumberFormatException ignored) {}

                    // 给邀请者奖励
                    Integer inviterBalance = pointLogService.getUserBalance(inviter.getId());
                    int inviterNewBalance = (inviterBalance != null ? inviterBalance : 0) + invitePoints;
                    PointLog inviterLog = PointLog.builder()
                            .userId(inviter.getId())
                            .type(2)
                            .amount(invitePoints)
                            .balance(inviterNewBalance)
                            .remark("邀请奖励: " + email)
                            .isSettled(1)
                            .createdAt(LocalDateTime.now())
                            .build();
                    pointLogService.save(inviterLog);

                    // 给被邀请者奖励
                    PointLog invitedLog = PointLog.builder()
                            .userId(user.getId())
                            .type(2)
                            .amount(invitePoints)
                            .balance(initPoints + invitePoints)
                            .remark("被邀请奖励")
                            .isSettled(1)
                            .createdAt(LocalDateTime.now())
                            .build();
                    pointLogService.save(invitedLog);
                }
            }

            // 生成 JWT Token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);

            return ApiResponse.success(data);
        } catch (Exception e) {
            log.error("注册失败", e);
            return ApiResponse.error("注册失败，请稍后重试");
        }
    }

    @PostMapping("/resetPassword/sendCode")
    @Operation(summary = "发送重置密码验证码")
    public ApiResponse<Void> resetPasswordSendCode(@RequestParam String email) {
        try {
            // 检查邮箱是否已注册
            SysUser user = sysUserService.findByEmail(email);
            if (user == null) {
                return ApiResponse.error(400, "该邮箱未注册");
            }

            // 生成 6 位验证码
            String code = RandomStringUtils.randomNumeric(6);

            // 保存验证码到数据库
            EmailCode emailCode = EmailCode.builder()
                    .email(email)
                    .code(code)
                    .expireAt(LocalDateTime.now().plusMinutes(10))
                    .used(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            emailCodeService.save(emailCode);

            // 发送邮件
            emailUtil.sendVerificationCode(email, code);

            return ApiResponse.success();
        } catch (Exception e) {
            log.error("发送重置密码验证码失败", e);
            return ApiResponse.error("发送验证码失败，请稍后重试");
        }
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码")
    public ApiResponse<Void> resetPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword) {
        try {
            // 验证验证码
            EmailCode emailCode = emailCodeService.getOne(
                    new QueryWrapper<EmailCode>()
                            .eq("email", email)
                            .eq("code", code)
                            .eq("used", 0)
                            .gt("expire_at", LocalDateTime.now())
                            .orderByDesc("created_at")
                            .last("LIMIT 1")
            );

            if (emailCode == null) {
                return ApiResponse.error(400, "验证码无效或已过期");
            }

            // 标记验证码为已使用
            emailCode.setUsed(1);
            emailCodeService.updateById(emailCode);

            // 查找用户并更新密码
            SysUser user = sysUserService.findByEmail(email);
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }

            // 密码长度校验
            if (newPassword == null || newPassword.length() < 6) {
                return ApiResponse.error(400, "密码长度至少6位");
            }

            user.setPassword(passwordEncoder.encode(decodePassword(newPassword)));
            user.setUpdatedAt(LocalDateTime.now());
            sysUserService.updateById(user);

            return ApiResponse.success();
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return ApiResponse.error("重置密码失败，请稍后重试");
        }
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息")
    public ApiResponse<SysUser> getUserInfo(@RequestAttribute Long userId) {
        try {
            SysUser user = sysUserService.getById(userId);
            if (user == null) {
                return ApiResponse.error(404, "用户不存在");
            }
            // 检查积分（根据提示词配置消耗积分）
            Integer balance = pointLogService.getUserBalance(userId);
            user.setPoints(balance != null ? balance : 0);

            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
