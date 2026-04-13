package com.example.house.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.house.modules.customer.entity.TenantEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantMapper extends BaseMapper<TenantEntity> {
}
