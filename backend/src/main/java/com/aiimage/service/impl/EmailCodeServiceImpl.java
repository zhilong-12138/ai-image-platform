package com.aiimage.service.impl;

import com.aiimage.entity.EmailCode;
import com.aiimage.mapper.EmailCodeMapper;
import com.aiimage.service.EmailCodeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EmailCodeServiceImpl extends ServiceImpl<EmailCodeMapper, EmailCode> implements EmailCodeService {
}
