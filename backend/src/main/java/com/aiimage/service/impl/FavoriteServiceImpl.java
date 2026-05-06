package com.aiimage.service.impl;

import com.aiimage.entity.Favorite;
import com.aiimage.mapper.FavoriteMapper;
import com.aiimage.service.FavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
}
