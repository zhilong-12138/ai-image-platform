package com.aiimage.service.impl;

import com.aiimage.entity.GenerationRecord;
import com.aiimage.mapper.GenerationRecordMapper;
import com.aiimage.service.GenerationRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class GenerationRecordServiceImpl extends ServiceImpl<GenerationRecordMapper, GenerationRecord> implements GenerationRecordService {
}
