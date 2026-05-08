package com.aiimage.config;

import com.aiimage.util.JwtUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        String rawPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = contextPath.equals("/") ? rawPath : rawPath.substring(contextPath.length());
        if (path.isEmpty()) path = "/";


        // /error 路径直接通过
        if (path.equals("/error") || path.startsWith("/error")) {
            return true;
        }

        // 公开路径：无须 token 验证（但仍会尝试提取 userId）
        if (path.equals("/user/login")
                || path.equals("/user/sendCode")
                || path.equals("/user/register")
                || path.equals("/admin/login")
                || path.startsWith("/category/")
                || path.startsWith("/upload/")
                || path.startsWith("/doc")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/")) {
            return true;
        }

        // /prompt/ 路径公开，但如果有 token 则提取 userId
        if (path.startsWith("/prompt/")) {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String email = jwtUtil.getEmailFromToken(token);
                    request.setAttribute("userId", userId);
                    request.setAttribute("email", email);
                }
            }
            return true;
        }

        // 所有其他路径（包括 /generation/**）需要有效 token 并设置 userId
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, 401, "Unauthorized");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            writeError(response, 401, "Unauthorized");
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String email = jwtUtil.getEmailFromToken(token);
        request.setAttribute("userId", userId);
        request.setAttribute("email", email);
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + msg + "\"}");
    }
}