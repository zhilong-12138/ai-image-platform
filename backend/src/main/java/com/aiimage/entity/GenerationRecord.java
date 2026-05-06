package com.aiimage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("generation_record")
public class GenerationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Long promptId;
    private String params;
    private String refImages;
    private Integer status;
    private String resultImages;
    private String externalApiResponse;
    private String errorMsg;
    private Integer costPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
