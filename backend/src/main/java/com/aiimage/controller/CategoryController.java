package com.aiimage.controller;

import com.aiimage.dto.ApiResponse;
import com.aiimage.entity.Category;
import com.aiimage.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@Tag(name = "分类", description = "提示词分类相关接口")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "分类列表")
    public ApiResponse<List<Category>> list() {
        List<Category> list = categoryService.list(
                new QueryWrapper<Category>()
                        .eq("status", 1)
                        .orderByAsc("sort")
                        .orderByAsc("id")
        );
        return ApiResponse.success(list);
    }
}
