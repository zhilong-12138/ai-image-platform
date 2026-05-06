package com.aiimage.service.impl;

import com.aiimage.entity.SysUser;
import com.aiimage.mapper.SysUserMapper;
import com.aiimage.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.RandomStringUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    
    @Override
    public SysUser findByEmail(String email) {
        return this.getOne(new QueryWrapper<SysUser>().eq("email", email));
    }
    
    @Override
    public String generateInviteCode() {
        return RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    }
}
