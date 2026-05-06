package com.aiimage.controller;

import com.aiimage.dto.ApiResponse;
import com.aiimage.util.OssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
@Tag(name = "文件上传", description = "图片等文件上传接口")
@RequiredArgsConstructor
public class FileController {

    private final OssUtil ossUtil;

    @PostMapping("/image")
    @Operation(summary = "上传图片")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossUtil.uploadFile(file.getInputStream(), file.getOriginalFilename());
            return ApiResponse.success(url);
        } catch (Exception e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/oss/upload")
    @Operation(summary = "OSS上传图片")
    public ApiResponse<String> uploadToOss(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossUtil.uploadFile(file.getInputStream(), file.getOriginalFilename());
            return ApiResponse.success(url);
        } catch (Exception e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }
}