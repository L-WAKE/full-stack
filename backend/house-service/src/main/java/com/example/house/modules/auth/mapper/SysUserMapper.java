package com.example.house.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.house.modules.auth.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {
}
