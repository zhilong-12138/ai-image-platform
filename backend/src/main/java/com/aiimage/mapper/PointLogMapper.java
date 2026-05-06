package com.aiimage.mapper;

import com.aiimage.entity.PointLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PointLogMapper extends BaseMapper<PointLog> {
    @Select("SELECT COALESCE(SUM(ABS(amount)), 0) FROM point_log WHERE type = 1")
    Integer getTotalCost();
}
