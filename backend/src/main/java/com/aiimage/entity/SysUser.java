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
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String email;
    private String password;
    private Integer level;
    private String inviteCode;
    private Long inviteeId;
    private Integer status;
    // 非数据库字段，供管理后台使用
    private transient Integer points;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
