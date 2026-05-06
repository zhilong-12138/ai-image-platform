package com.aiimage.service.impl;

import com.aiimage.entity.SystemConfig;
import com.aiimage.mapper.SystemConfigMapper;
import com.aiimage.service.SystemConfigService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public SystemConfig getByKey(String key) {
        return getOne(new QueryWrapper<SystemConfig>().eq("config_key", key));
    }

    @Override
    public String getValue(String key) {
        SystemConfig config = getByKey(key);
        return config != null ? config.getConfigValue() : "";
    }
}
