package com.campus.help.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 聊天消息实体（成员 C）。type: 0 文本 1 图片 2 订单卡片。
 */
@Entity(tableName = "messages")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String conversationId; // 会话 ID（双方 userId 组合 / 任务 ID）

    public long senderId;

    public long receiverId;

    public String content;

    public int type;            // 0 文本 1 图片 2 订单卡片

    public long timestamp;

    public boolean read;
}
