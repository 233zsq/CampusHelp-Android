package com.campus.help.core.utils;

/**
 * 会话 ID 工具（1 对 1 私聊约定，前后端共用）：
 * conversationId = min(a,b) + "_" + max(a,b)。
 */
public final class ConversationId {

    private ConversationId() {
    }

    public static String of(long a, long b) {
        return Math.min(a, b) + "_" + Math.max(a, b);
    }

    /** 从会话 ID 与「自己」反推对方 userId；解析失败返回 0。 */
    public static long peerOf(String conversationId, long me) {
        if (conversationId == null) {
            return 0;
        }
        int idx = conversationId.indexOf('_');
        if (idx <= 0) {
            return 0;
        }
        try {
            long x = Long.parseLong(conversationId.substring(0, idx));
            long y = Long.parseLong(conversationId.substring(idx + 1));
            return x == me ? y : x;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
