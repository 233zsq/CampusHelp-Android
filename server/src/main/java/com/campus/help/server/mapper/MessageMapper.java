package com.campus.help.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.help.server.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息 Mapper — 替代 Android 端 MessageDao (Room @Dao)。
 */
@Mapper
public interface MessageMapper extends BaseMapper<ChatMessage> {
}
