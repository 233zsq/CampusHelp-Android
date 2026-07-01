package com.campus.help.server.ws;

import com.campus.help.server.entity.ChatMessage;
import com.campus.help.server.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自研 IM WebSocket 处理器（原生 WebSocket，非 STOMP）。
 * <p>
 * 消息收发约定：
 * <ul>
 *   <li>客户端发送 payload {@code {seq, to, content, type}}（from 由服务端从握手 userId 覆盖）。</li>
 *   <li>服务端落库后向<b>发送方</b>回 {@code {ack:true, seq, id, conversationId, from, to, content, type, timestamp}}，
 *       客户端据此把乐观插入的临时消息替换为真实 id。</li>
 *   <li>向<b>接收方</b>在线 session 推 {@code {id, conversationId, from, to, content, type, timestamp}}（不带 seq）；离线只落库。</li>
 *   <li>conversationId = min(uid1,uid2) + "_" + max(uid1,uid2)。</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    /** userId → 在线 session。value 用 session 本身做存在性校验。 */
    private static final Map<Long, WebSocketSession> ONLINE = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("WS 连接缺少 userId attribute，关闭");
            closeQuietly(session);
            return;
        }
        // 顶掉旧 session（同账号多端登录时新连接替换旧连接）
        WebSocketSession old = ONLINE.put(userId, session);
        if (old != null && old.isOpen()) {
            closeQuietly(old);
        }
        log.info("WS 上线: userId={}, 当前在线={}", userId, ONLINE.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long from = (Long) session.getAttributes().get("userId");
        if (from == null) {
            closeQuietly(session);
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);

            Object toObj = payload.get("to");
            if (toObj == null) {
                log.warn("WS 消息缺少 to, from={}", from);
                return;
            }
            if (!(toObj instanceof Number)) {
                log.warn("WS 消息 to 不是数字类型 ({}), from={}", toObj.getClass(), from);
                return;
            }
            long to = ((Number) toObj).longValue();
            Object content = payload.get("content");
            Object typeObj = payload.get("type");
            int type = typeObj instanceof Number n ? n.intValue() : 0;
            Object seqObj = payload.get("seq");
            if (seqObj != null && !(seqObj instanceof Number)) {
                log.warn("WS 消息 seq 不是数字类型 ({}), from={}", seqObj.getClass(), from);
                seqObj = null;
            }

            // 构造并落库
            ChatMessage msg = new ChatMessage();
            msg.setConversationId(convId(from, to));
            msg.setSenderId(from);
            msg.setReceiverId(to);
            msg.setContent(content == null ? "" : content.toString());
            msg.setType(type);
            msg.setReadStatus(false);
            Long id = messageService.send(msg); // 落库 + 填 timestamp + id

            // 规范化消息（不含 seq）
            Map<String, Object> canonical = new HashMap<>();
            canonical.put("id", id);
            canonical.put("conversationId", msg.getConversationId());
            canonical.put("from", from);
            canonical.put("to", to);
            canonical.put("content", msg.getContent());
            canonical.put("type", type);
            canonical.put("timestamp", msg.getTimestamp());

            // 回 ACK 给发送方（带 seq，让客户端替换临时消息）
            Map<String, Object> ack = new HashMap<>(canonical);
            ack.put("ack", true);
            if (seqObj != null) {
                ack.put("seq", seqObj);
            }
            sendToUser(from, ack);

            // 推给接收方（不带 seq）
            sendToUser(to, canonical);

        } catch (Exception e) {
            log.error("WS 处理消息失败, from={}", from, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE.remove(userId, session); // 仅当 value 仍是该 session 才移除
            log.info("WS 下线: userId={}, 当前在线={}", userId, ONLINE.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.warn("WS 传输错误, userId={}: {}", userId, exception.getMessage());
        closeQuietly(session);
    }

    /** 给指定用户发消息；离线或发送失败静默处理（消息已落库）。 */
    private void sendToUser(Long userId, Object payload) {
        WebSocketSession session = ONLINE.get(userId);
        if (session == null || !session.isOpen()) {
            return; // 离线，只落库
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            log.warn("WS 推送失败, userId={}: {}", userId, e.getMessage());
            ONLINE.remove(userId, session);
            closeQuietly(session);
        }
    }

    private static String convId(long a, long b) {
        return Math.min(a, b) + "_" + Math.max(a, b);
    }

    private static void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (Exception ignored) {
        }
    }
}