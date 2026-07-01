package com.campus.help.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.help.server.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接单 Mapper — 替代 Android 端 OrderDao (Room @Dao)。
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
