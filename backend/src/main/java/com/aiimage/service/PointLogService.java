package com.aiimage.service;

import com.aiimage.entity.PointLog;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PointLogService extends IService<PointLog> {
    Integer getUserBalance(Long userId);
    Integer getTotalCost();
}
