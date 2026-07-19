package com.youlai.boot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.system.model.entity.App;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用 Mapper（平台级表，已加入租户忽略列表）
 */
@Mapper
public interface AppMapper extends BaseMapper<App> {
}