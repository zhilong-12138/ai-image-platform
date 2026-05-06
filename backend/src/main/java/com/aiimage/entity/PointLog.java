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
@TableName("point_log")
public class PointLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Integer type;
    private Integer amount;
    private Integer balance;
    private Long relatedId;
    private String remark;
    private Integer isSettled;
    private LocalDateTime createdAt;
}
