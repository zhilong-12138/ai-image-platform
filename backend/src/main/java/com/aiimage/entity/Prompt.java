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
@TableName("prompt")
public class Prompt {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long categoryId;
    private String title;
    private String description;
    private String content;
    private String imageUrls;
    private String params;
    private Integer lifeCycle;
    private String apiUrl;
    private String apiMethod;
    private String apiParams;
    private Integer costPoints;
    private Integer refImageCount;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
