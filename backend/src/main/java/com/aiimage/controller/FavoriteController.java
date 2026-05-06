package com.aiimage.controller;

import com.aiimage.dto.ApiResponse;
import com.aiimage.entity.Favorite;
import com.aiimage.entity.Prompt;
import com.aiimage.service.FavoriteService;
import com.aiimage.service.PromptService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fav")
@Tag(name = "收藏", description = "收藏相关接口")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final PromptService promptService;

    @GetMapping("/favorites")
    @Operation(summary = "我的收藏列表")
    public ApiResponse<List<Prompt>> list(@RequestAttribute Long userId) {
        List<Favorite> favorites = favoriteService.list(
                new QueryWrapper<Favorite>().eq("user_id", userId).orderByDesc("created_at")
        );

        List<Prompt> prompts = favorites.stream().map(fav -> {
            Prompt prompt = promptService.getById(fav.getPromptId());
            return prompt;
        }).toList();

        return ApiResponse.success(prompts);
    }

    @PostMapping("/favorite")
    @Operation(summary = "添加收藏")
    public ApiResponse<Void> add(@RequestAttribute Long userId, @RequestParam Long promptId) {
        Favorite existing = favoriteService.getOne(
                new QueryWrapper<Favorite>().eq("user_id", userId).eq("prompt_id", promptId)
        );
        if (existing != null) {
            return ApiResponse.error(400, "已收藏");
        }

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .promptId(promptId)
                .build();
        favoriteService.save(favorite);
        return ApiResponse.success();
    }

    @DeleteMapping("/favorite/{promptId}")
    @Operation(summary = "取消收藏")
    public ApiResponse<Void> remove(@RequestAttribute Long userId, @PathVariable Long promptId) {
        favoriteService.remove(
                new QueryWrapper<Favorite>().eq("user_id", userId).eq("prompt_id", promptId)
        );
        return ApiResponse.success();
    }

    @GetMapping("/favorite/check")
    @Operation(summary = "检查是否已收藏")
    public ApiResponse<Boolean> check(@RequestAttribute Long userId, @RequestParam Long promptId) {
        Favorite existing = favoriteService.getOne(
                new QueryWrapper<Favorite>().eq("user_id", userId).eq("prompt_id", promptId)
        );
        return ApiResponse.success(existing != null);
    }
}