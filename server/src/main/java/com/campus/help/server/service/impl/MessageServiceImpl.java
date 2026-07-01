package com.campus.help.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.help.server.entity.ChatMessage;
import com.campus.help.server.mapper.MessageMapper;
import com.campus.help.server.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public Long send(ChatMessage message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(System.currentTimeMillis());
        }
        if (message.getRead() == null) {
            message.setRead(false);
        }
        messageMapper.insert(message);
        return message.getId();
    }

    @Override
    public List<ChatMessage> listByConversation(String conversationId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .orderByAsc(ChatMessage::getTimestamp)
        );
    }

    @Override
    public List<ChatMessage> listAllByReceiver(Long receiverId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getReceiverId, receiverId)
                        .orderByDesc(ChatMessage::getTimestamp)
        );
    }

    @Override
    public void markRead(String conversationId) {
        messageMapper.update(null,
                new LambdaUpdateWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .set(ChatMessage::getRead, true)
        );
    }
}
