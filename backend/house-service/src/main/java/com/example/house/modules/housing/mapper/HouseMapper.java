package com.example.house.modules.housing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.house.modules.housing.entity.HouseEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HouseMapper extends BaseMapper<HouseEntity> {
}
