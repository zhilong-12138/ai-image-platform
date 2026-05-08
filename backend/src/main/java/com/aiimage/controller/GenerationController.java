package com.aiimage.controller;

import com.aiimage.dto.ApiResponse;
import com.aiimage.entity.GenerationRecord;
import com.aiimage.entity.PointLog;
import com.aiimage.entity.Prompt;
import com.aiimage.service.GenerationRecordService;
import com.aiimage.service.PointLogService;
import com.aiimage.service.PromptService;
import com.aiimage.task.GenerationTask;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/generation")
@Tag(name = "图片生成", description = "生成任务提交、查询等接口")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationRecordService generationRecordService;
    private final PromptService promptService;
    private final PointLogService pointLogService;
    private final GenerationTask generationTask;

    @PostMapping("/submit")
    @Operation(summary = "提交生成任务")
    public ApiResponse<GenerationRecord> submit(
            @RequestAttribute Long userId,
            @RequestBody GenerationRecord record) {
        // 检查提示词是否存在
        Prompt prompt = promptService.getById(record.getPromptId());
        if (prompt == null) {
            return ApiResponse.error(404, "提示词不存在");
        }
        if (prompt.getStatus() != 1) {
            return ApiResponse.error(400, "该提示词已下架");
        }

        // 检查积分（根据提示词配置消耗积分）
        Integer balance = pointLogService.getUserBalance(userId);
        int currentBalance = balance != null ? balance : 0;
        Integer promptCost = prompt.getCostPoints();
        int cost = (promptCost != null && promptCost > 0) ? promptCost : 10;
        if (currentBalance < cost) {
            return ApiResponse.error(400, "积分不足");
        }

        // 创建生成记录
        GenerationRecord newRecord = GenerationRecord.builder()
                .userId(userId)
                .promptId(record.getPromptId())
                .params(record.getParams())
                .refImages(record.getRefImages())
                .status(1) // 生成中
                .costPoints(cost)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        generationRecordService.save(newRecord);

        // 异步执行生成任务
        generationTask.processGeneration(newRecord.getId());

        // 扣除积分
        int newBalance = currentBalance - cost;
        PointLog pointLog = PointLog.builder()
                .userId(userId)
                .type(1) // 生成消费
                .amount(-cost)
                .balance(newBalance)
                .remark("生成图片消耗")
                .isSettled(1)
                .createdAt(LocalDateTime.now())
                .build();
        pointLogService.save(pointLog);

        return ApiResponse.success(newRecord);
    }

    @GetMapping("/list")
    @Operation(summary = "生成记录列表")
    public ApiResponse<Page<GenerationRecord>> list(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<GenerationRecord> result = generationRecordService.page(
                new Page<>(page, pageSize),
                new QueryWrapper<GenerationRecord>()
                        .select("id", "user_id", "prompt_id", "params", "ref_images", "status",
                                "result_images", "error_msg", "cost_points", "created_at", "updated_at")
                        .eq("user_id", userId)
                        .orderByDesc("created_at")
        );
        return ApiResponse.success(result);
    }

    @GetMapping("/status/{id}")
    @Operation(summary = "查询生成状态")
    public ApiResponse<GenerationRecord> getStatus(
            @RequestAttribute Long userId,
            @PathVariable Long id) {
        GenerationRecord record = generationRecordService.getOne(
                new QueryWrapper<GenerationRecord>()
                        .select("id", "user_id", "prompt_id", "params", "ref_images", "status",
                                "result_images", "error_msg", "cost_points", "created_at", "updated_at")
                        .eq("id", id)
                        .eq("user_id", userId)
        );
        if (record == null) {
            return ApiResponse.error(404, "记录不存在");
        }
        return ApiResponse.success(record);
    }

    @PostMapping("/retry/{id}")
    @Operation(summary = "重试生成任务")
    public ApiResponse<Void> retry(@RequestAttribute Long userId, @PathVariable Long id) {
        GenerationRecord record = generationRecordService.getOne(
                new QueryWrapper<GenerationRecord>()
                        .select("id", "user_id", "prompt_id", "params", "ref_images", "status",
                                "result_images", "error_msg", "cost_points", "created_at", "updated_at")
                        .eq("id", id)
                        .eq("user_id", userId)
        );
        if (record == null) {
            return ApiResponse.error(404, "记录不存在");
        }
        if (record.getStatus() != 3) {
            return ApiResponse.error(400, "仅失败任务支持重试");
        }
        record.setStatus(1);
        record.setErrorMsg(null);
        record.setExternalApiResponse(null);
        record.setResultImages(null);
        record.setUpdatedAt(LocalDateTime.now());
        generationRecordService.updateById(record);

        // 异步执行生成任务
        generationTask.processGeneration(record.getId());
        return ApiResponse.success();
    }

    @GetMapping("/count")
    @Operation(summary = "查询用户生成图片总数")
    public ApiResponse<Integer> countByUserId(@RequestAttribute Long userId) {
        Long count = generationRecordService.count(
                new QueryWrapper<GenerationRecord>()
                        .select("id")
                        .eq("user_id", userId)
        );
        return ApiResponse.success(count.intValue());
    }
}
