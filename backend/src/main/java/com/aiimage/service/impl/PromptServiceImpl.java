package com.aiimage.service.impl;

import com.aiimage.entity.Prompt;
import com.aiimage.mapper.PromptMapper;
import com.aiimage.service.PromptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PromptServiceImpl extends ServiceImpl<PromptMapper, Prompt> implements PromptService {
}
