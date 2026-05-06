package com.aiimage.service;

import com.aiimage.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysUserService extends IService<SysUser> {
    SysUser findByEmail(String email);
    String generateInviteCode();
}
