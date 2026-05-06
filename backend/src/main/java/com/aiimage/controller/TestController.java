package com.aiimage.controller;

import com.aiimage.dto.ApiResponse;
import com.aiimage.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {
    private final JwtUtil jwtUtil;
    public TestController(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @GetMapping("/gentoken")
    public ApiResponse<String> gen(@RequestParam Long userId, @RequestParam String email) {
        return ApiResponse.success(jwtUtil.generateToken(userId, email));
    }

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return ApiResponse.success("pong");
    }
}
