package com.campus.help.server.controller;

import com.campus.help.server.common.Result;
import com.campus.help.server.entity.ChatMessage;
import com.campus.help.server.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 发送消息。
     * POST /api/messages
     */
    @PostMapping
    public Result<Long> send(@RequestBody ChatMessage message) {
        if (message.getConversationId() == null || message.getSenderId() == null) {
            return Result.fail(400, "conversationId 和 senderId 不能为空");
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
     * 标记会话已读。
     * PUT /api/messages/read?conversationId=xxx
     */
    @PutMapping("/read")
    public Result<Void> markRead(@RequestParam String conversationId) {
        messageService.markRead(conversationId);
        return Result.ok();
    }
}
