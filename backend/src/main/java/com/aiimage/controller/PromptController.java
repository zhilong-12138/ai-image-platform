package com.aiimage.controller;

import com.aiimage.dto.ApiResponse;
import com.aiimage.entity.Prompt;
import com.aiimage.service.PromptService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/prompt")
@Tag(name = "提示词管理", description = "提示词列表、详情等接口")
@RequiredArgsConstructor
public class PromptController {
    
    private final PromptService promptService;
    
    @GetMapping("/list")
    @Operation(summary = "提示词列表（分页）")
    public ApiResponse<Page<Prompt>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String description) {
        QueryWrapper<Prompt> wrapper = new QueryWrapper<Prompt>()
                .eq("status", 1)
                .orderByDesc("created_at");
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        if (StringUtils.isNotBlank(description)) {
            wrapper.like("description", description);
        }
        Page<Prompt> result = promptService.page(
                new Page<>(page, pageSize),
                wrapper
        );
        return ApiResponse.success(result);
    }
    
    @GetMapping("/detail/{id}")
    @Operation(summary = "提示词详情")
    public ApiResponse<Prompt> detail(@PathVariable Long id) {
        Prompt prompt = promptService.getById(id);
        return ApiResponse.success(prompt);
    }
}
