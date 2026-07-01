package com.campus.help.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.help.server.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper — 替代 Android 端 UserDao (Room @Dao)。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
