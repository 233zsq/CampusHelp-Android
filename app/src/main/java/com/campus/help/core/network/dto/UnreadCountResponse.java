package com.campus.help.core.network.dto;

/**
 * 未读消息计数响应（对应后端 GET /api/messages/unread/count 返回的 data: { "total": N }）。
 */
public class UnreadCountResponse {
    public long total;
}
