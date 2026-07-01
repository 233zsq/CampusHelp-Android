package com.campus.help.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 聊天消息实体 — 对应 Android 端 com.campus.help.data.model.ChatMessage + Room messages 表。
 * type: 0 文本 1 图片 2 订单卡片。
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话 ID（双方 userId 组合 / 任务 ID） */
    private String conversationId;

    /** 发送者 user.id */
    private Long senderId;

    /** 接收者 user.id */
    private Long receiverId;

    /** 消息内容 */
    private String content;

    /** 0 文本 1 图片 2 订单卡片 */
    private Integer type;

    /** 发送时间戳 (ms) */
    private Long timestamp;

    /**
     * 是否已读。
     * 字段名 isRead、列名 is_read：避开 MySQL 保留字 read。
     * （若字段名仍叫 read，MyBatis-Plus 会生成 "is_read AS read"，别名 read 仍是保留字 → 语法错。）
     * 字段名取 isRead 正好匹配 is_read 的下划线转驼峰，自定义 @Select(m.*) 的结果映射也能自动对上。
     * @JsonProperty("read") 保持对客户端 JSON 字段名 read 不变。
     */
    @TableField("is_read")
    @JsonProperty("read")
    private Boolean isRead;

    @TableLogic
    private Integer deleted;
}
