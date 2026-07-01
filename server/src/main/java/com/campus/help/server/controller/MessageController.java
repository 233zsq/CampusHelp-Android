package com.campus.help.server.controller;

import com.campus.help.server.common.Result;
import com.campus.help.server.entity.ChatMessage;
import com.campus.help.server.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 消息控制器。
 * 对应 Android 端 MessageRepository 的远端数据源。
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送消息（REST 兜底；实时路径走 WebSocket /ws）。
     * senderId 强制取登录用户，防越权。
     * POST /api/messages
     */
    @PostMapping
    public Result<Long> send(@RequestBody ChatMessage message,
                             @RequestAttribute("currentUserId") Long currentUserId) {
        if (message.getReceiverId() == null) {
            return Result.fail(400, "receiverId 不能为空");
        }
        message.setSenderId(currentUserId);
        if (message.getConversationId() == null || message.getConversationId().isBlank()) {
            message.setConversationId(convId(currentUserId, message.getReceiverId()));
        }
        Long id = messageService.send(message);
        return Result.ok(id);
    }

    /**
     * 查询会话消息列表。
     * GET /api/messages?conversationId=xxx
     */
    @GetMapping
    public Result<List<ChatMessage>> list(
            @RequestParam(required = false) String conversationId,
            @RequestParam(required = false) Long receiverId) {

        if (conversationId != null) {
            return Result.ok(messageService.listByConversation(conversationId));
        } else if (receiverId != null) {
            return Result.ok(messageService.listAllByReceiver(receiverId));
        }
        return Result.fail(400, "请提供 conversationId 或 receiverId");
    }

    /**
     * 当前用户的会话列表（每个会话最新一条消息，按时间倒序）。
     * GET /api/messages/conversations
     */
    @GetMapping("/conversations")
    public Result<List<ChatMessage>> conversations(@RequestAttribute("currentUserId") Long currentUserId) {
        return Result.ok(messageService.listConversations(currentUserId));
    }

    /**
     * 当前用户的未读消息总数（用于底部 tab 角标）。
     * GET /api/messages/unread/count
     */
    @GetMapping("/unread/count")
    public Result<Map<String, Object>> unreadCount(@RequestAttribute("currentUserId") Long currentUserId) {
        long total = messageService.unreadCount(currentUserId);
        return Result.ok(Map.of("total", total));
    }

    /**
     * 标记会话已读：只把当前用户作为接收者的消息置为已读。
     * PUT /api/messages/read?conversationId=xxx
     */
    @PutMapping("/read")
    public Result<Void> markRead(@RequestParam String conversationId,
                                 @RequestAttribute("currentUserId") Long currentUserId) {
        messageService.markRead(conversationId, currentUserId);
        return Result.ok();
    }

    private static String convId(long a, long b) {
        return Math.min(a, b) + "_" + Math.max(a, b);
    }
}
