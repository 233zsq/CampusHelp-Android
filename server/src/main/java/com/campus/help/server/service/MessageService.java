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
     * 标记会话已读：只把 receiverId = receiverId 的消息置为已读，
     * 避免把发送方自己发出的消息也标成已读（否则会伪造对方已读）。
     */
    void markRead(String conversationId, Long receiverId);

    /**
     * 当前用户参与的所有会话的最新一条消息（按时间倒序）。
     */
    List<ChatMessage> listConversations(Long userId);

    /**
     * 当前用户作为接收者的未读消息总数。
     */
    long unreadCount(Long userId);
}
