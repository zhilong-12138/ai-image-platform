package com.aiimage.controller;

import com.aiimage.dto.ApiResponse;
import com.aiimage.entity.Category;
import com.aiimage.entity.GenerationRecord;
import com.aiimage.entity.Prompt;
import com.aiimage.entity.PointLog;
import com.aiimage.entity.SysUser;
import com.aiimage.entity.SystemConfig;
import com.aiimage.service.CategoryService;
import com.aiimage.service.GenerationRecordService;
import com.aiimage.service.PointLogService;
import com.aiimage.service.PromptService;
import com.aiimage.service.SysUserService;
import com.aiimage.service.SystemConfigService;
import com.aiimage.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "管理后台", description = "管理员专用接口")
@RequiredArgsConstructor
public class AdminController {

    private final SysUserService sysUserService;
    private final PromptService promptService;
    private final GenerationRecordService generationRecordService;
    private final PointLogService pointLogService;
    private final SystemConfigService systemConfigService;
    private final CategoryService categoryService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public ApiResponse<Map<String, Object>> login(@RequestParam String email) {
        // 管理员白名单：指定邮箱才能登录
        // 可配置在 system_config 表中，或直接写死
        String adminEmails = systemConfigService.getByKey("admin_emails") != null
                ? systemConfigService.getByKey("admin_emails").getConfigValue()
                : "";
        // 默认允许的 admin 邮箱
        if (adminEmails.isEmpty()) {
            adminEmails = "admin@aiimage.com,admin@example.com";
        }

        boolean isAdmin = false;
        for (String adminEmail : adminEmails.split(",")) {
            if (adminEmail.trim().equalsIgnoreCase(email.trim())) {
                isAdmin = true;
                break;
            }
        }

        if (!isAdmin) {
            return ApiResponse.error(403, "无权限访问管理后台");
        }

        // 查找或创建管理员用户
        SysUser user = sysUserService.findByEmail(email);
        if (user == null) {
            user = SysUser.builder()
                    .email(email)
                    .level(99)
                    .inviteCode(sysUserService.generateInviteCode())
                    .status(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            sysUserService.save(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return ApiResponse.success(data);
    }

    @GetMapping("/stats")
    @Operation(summary = "仪表板统计数据")
    public ApiResponse<Map<String, Object>> getStats() {
        long totalUsers = sysUserService.count();
        long totalGenerations = generationRecordService.count();
        long pendingImages = generationRecordService.count(
                new QueryWrapper<GenerationRecord>().eq("status", 0)
        );
        // 总积分消耗 = 所有消费积分的绝对值之和
        Integer totalCost = pointLogService.getTotalCost();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalGenerations", totalGenerations);
        stats.put("pendingImages", pendingImages);
        stats.put("totalCost", totalCost != null ? totalCost : 0);
        return ApiResponse.success(stats);
    }

    @GetMapping("/users")
    @Operation(summary = "用户列表")
    public ApiResponse<Page<SysUser>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String email) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (email != null && !email.isBlank()) {
            wrapper.like("email", email);
        }
        wrapper.orderByDesc("created_at");
        Page<SysUser> result = sysUserService.page(new Page<>(page, pageSize), wrapper);
        // 计算每个用户的当前积分
        for (SysUser user : result.getRecords()) {
            Integer balance = pointLogService.getUserBalance(user.getId());
            user.setPoints(balance != null ? balance : 0);
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/users/{userId}/points")
    @Operation(summary = "调整用户积分")
    public ApiResponse<Void> updatePoints(
            @PathVariable Long userId,
            @RequestParam Integer amount) {
        // 获取当前余额
        Integer currentBalance = pointLogService.getUserBalance(userId);
        int balance = currentBalance != null ? currentBalance : 0;
        int newBalance = balance + amount;

        // 记录积分变动
        PointLog pointLog = PointLog.builder()
                .userId(userId)
                .type(4) // 管理员调整
                .amount(amount)
                .balance(newBalance)
                .remark("管理员调整")
                .isSettled(1)
                .build();
        pointLogService.save(pointLog);

        return ApiResponse.success();
    }

    @PostMapping("/users/{userId}/disable")
    @Operation(summary = "禁用/启用用户")
    public ApiResponse<Void> disableUser(@PathVariable Long userId, @RequestParam Integer status) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        user.setStatus(status);
        sysUserService.updateById(user);
        return ApiResponse.success();
    }

    @GetMapping("/prompts")
    @Operation(summary = "提示词列表（管理）")
    public ApiResponse<Page<Prompt>> listPrompts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Prompt> result = promptService.page(
                new Page<>(page, pageSize),
                new QueryWrapper<Prompt>().orderByDesc("created_at")
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/categories")
    @Operation(summary = "分类列表（管理）")
    public ApiResponse<List<Category>> listCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer status) {
        QueryWrapper<Category> wrapper = new QueryWrapper<Category>()
                .orderByAsc("sort")
                .orderByAsc("id");
        if (name != null && !name.isBlank()) {
            wrapper.like("name", name);
        }
        if (code != null && !code.isBlank()) {
            wrapper.like("code", code);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        return ApiResponse.success(categoryService.list(wrapper));
    }

    @PostMapping("/categories")
    @Operation(summary = "创建分类")
    public ApiResponse<Void> createCategory(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return ApiResponse.error(400, "分类名称不能为空");
        }
        if (category.getCode() == null || category.getCode().isBlank()) {
            return ApiResponse.error(400, "分类编码不能为空");
        }
        Long exist = categoryService.count(new QueryWrapper<Category>().eq("code", category.getCode()));
        if (exist != null && exist > 0) {
            return ApiResponse.error(400, "分类编码已存在");
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        categoryService.save(category);
        return ApiResponse.success();
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "更新分类")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category existing = categoryService.getById(id);
        if (existing == null) {
            return ApiResponse.error(404, "分类不存在");
        }
        if (category.getName() != null && category.getName().isBlank()) {
            return ApiResponse.error(400, "分类名称不能为空");
        }
        if (category.getCode() != null && category.getCode().isBlank()) {
            return ApiResponse.error(400, "分类编码不能为空");
        }
        if (category.getCode() != null && !category.getCode().equals(existing.getCode())) {
            Long exist = categoryService.count(new QueryWrapper<Category>().eq("code", category.getCode()).ne("id", id));
            if (exist != null && exist > 0) {
                return ApiResponse.error(400, "分类编码已存在");
            }
        }
        category.setId(id);
        categoryService.updateById(category);
        return ApiResponse.success();
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "删除分类")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return ApiResponse.success();
    }

    @PostMapping("/prompts")
    @Operation(summary = "创建提示词")
    public ApiResponse<Void> createPrompt(@RequestBody Prompt prompt) {
        promptService.save(prompt);
        return ApiResponse.success();
    }

    @PutMapping("/prompts/{id}")
    @Operation(summary = "更新提示词")
    public ApiResponse<Void> updatePrompt(@PathVariable Long id, @RequestBody Prompt prompt) {
        prompt.setId(id);
        promptService.updateById(prompt);
        return ApiResponse.success();
    }

    @DeleteMapping("/prompts/{id}")
    @Operation(summary = "删除提示词")
    public ApiResponse<Void> deletePrompt(@PathVariable Long id) {
        promptService.removeById(id);
        return ApiResponse.success();
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "用户详情")
    public ApiResponse<SysUser> getUserDetail(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return ApiResponse.error(404, "用户不存在");
        Integer balance = pointLogService.getUserBalance(id);
        user.setPoints(balance != null ? balance : 0);
        return ApiResponse.success(user);
    }

    @GetMapping("/point-logs")
    @Operation(summary = "积分记录列表")
    public ApiResponse<Page<PointLog>> listPointLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId) {
        QueryWrapper<PointLog> wrapper = new QueryWrapper<>();
        if (userId != null) wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_at");
        return ApiResponse.success(pointLogService.page(new Page<>(page, pageSize), wrapper));
    }

    @PostMapping("/prompts/{id}/publish")
    @Operation(summary = "发布/下架提示词")
    public ApiResponse<Void> publishPrompt(@PathVariable Long id, @RequestParam Integer status) {
        Prompt prompt = promptService.getById(id);
        if (prompt == null) {
            return ApiResponse.error(404, "提示词不存在");
        }
        prompt.setStatus(status);
        promptService.updateById(prompt);
        return ApiResponse.success();
    }

    @GetMapping("/images")
    @Operation(summary = "待审核图片列表")
    public ApiResponse<Page<GenerationRecord>> listImages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<GenerationRecord> result = generationRecordService.page(
                new Page<>(page, pageSize),
                new QueryWrapper<GenerationRecord>()
                        .select("id", "user_id", "prompt_id", "params", "ref_images", "status",
                                "result_images", "error_msg", "cost_points", "created_at", "updated_at")
                        .eq("status", 2) // 已生成完成
                        .orderByDesc("created_at")
        );
        return ApiResponse.success(result);
    }

    @PostMapping("/images/{id}/approve")
    @Operation(summary = "批准图片")
    public ApiResponse<Void> approveImage(@PathVariable Long id) {
        GenerationRecord record = generationRecordService.getById(id);
        if (record == null) {
            return ApiResponse.error(404, "记录不存在");
        }
        // TODO: 可以在这里标记审核通过状态
        return ApiResponse.success();
    }

    @PostMapping("/images/{id}/reject")
    @Operation(summary = "拒绝图片")
    public ApiResponse<Void> rejectImage(@PathVariable Long id, @RequestParam(required = false) String reason) {
        GenerationRecord record = generationRecordService.getById(id);
        if (record == null) {
            return ApiResponse.error(404, "记录不存在");
        }
        record.setStatus(3); // 标记为失败
        record.setErrorMsg(reason);
        generationRecordService.updateById(record);
        return ApiResponse.success();
    }

    @GetMapping("/config")
    @Operation(summary = "获取系统配置")
    public ApiResponse<List<SystemConfig>> getAllConfig() {
        List<SystemConfig> configs = systemConfigService.list();
        return ApiResponse.success(configs);
    }

    @PutMapping("/config/{key}")
    @Operation(summary = "更新系统配置")
    public ApiResponse<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        SystemConfig config = systemConfigService.getByKey(key);
        if (config == null) {
            return ApiResponse.error(404, "配置项不存在");
        }
        config.setConfigValue(value);
        systemConfigService.updateById(config);
        return ApiResponse.success();
    }
}
