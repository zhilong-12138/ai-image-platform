package com.aiimage.service.impl;

import com.aiimage.entity.PointLog;
import com.aiimage.mapper.PointLogMapper;
import com.aiimage.service.PointLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PointLogServiceImpl extends ServiceImpl<PointLogMapper, PointLog> implements PointLogService {

    @Override
    public Integer getUserBalance(Long userId) {
        // 获取该用户最近一条积分记录的余额作为当前余额
        PointLog lastLog = getOne(
                new QueryWrapper<PointLog>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at")
                        .last("LIMIT 1")
        );
        return lastLog != null ? lastLog.getBalance() : null;
    }

    @Override
    public Integer getTotalCost() {
        // 统计所有消费类型的积分绝对值之和
        return baseMapper.getTotalCost();
    }
}
