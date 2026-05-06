package com.aiimage.service;

import com.aiimage.entity.SystemConfig;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SystemConfigService extends IService<SystemConfig> {
    SystemConfig getByKey(String key);
    String getValue(String key);
}
