package com.aiimage.service.impl;

import com.aiimage.entity.Category;
import com.aiimage.mapper.CategoryMapper;
import com.aiimage.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
