package com.campus.help.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.help.server.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息 Mapper — 替代 Android 端 MessageDao (Room @Dao)。
 */
@Mapper
public interface MessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 当前用户参与的每个会话的最新一条消息（用于会话列表），按时间倒序。
     * 取每个 conversation_id 下 timestamp 最大的一条。
     */
    @Select("SELECT m.* FROM chat_message m " +
            "INNER JOIN (" +
            "  SELECT conversation_id, MAX(timestamp) AS max_ts " +
            "  FROM chat_message " +
            "  WHERE (sender_id = #{userId} OR receiver_id = #{userId}) AND deleted = 0 " +
            "  GROUP BY conversation_id" +
            ") t ON m.conversation_id = t.conversation_id AND m.timestamp = t.max_ts " +
            "WHERE m.deleted = 0 " +
            "ORDER BY m.timestamp DESC")
    List<ChatMessage> selectLatestPerConversation(@Param("userId") Long userId);

    /**
     * 当前用户作为接收者且未读的消息总数（用于底部 tab 角标）。
     * 列名 is_read（原 read 是 MySQL 保留字，已迁移）。
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE receiver_id = #{userId} AND is_read = 0 AND deleted = 0")
    long selectUnreadCount(@Param("userId") Long userId);
}
