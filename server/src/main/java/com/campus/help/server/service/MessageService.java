package com.campus.help.server.service;

import com.campus.help.server.entity.ChatMessage;

import java.util.List;

/**
 * 消息 Service。
 */
public interface MessageService {

    /**
     * 发送消息。
     */
    Long send(ChatMessage message);

    /**
     * 查询会话消息列表（按时间正序）。
     */
    List<ChatMessage> listByConversation(String conversationId);

    /**
     * 查询用户的所有最新消息（按时间倒序）。
     */
    List<ChatMessage> listAllByReceiver(Long receiverId);

    /**
     * 标记会话已读。
     */
    void markRead(String conversationId);
}
