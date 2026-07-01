package com.campus.help.server.ws;

import com.campus.help.server.common.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 握手鉴权拦截器。
 * <p>
 * 浏览器/OkHttp 在 WS 握手阶段不便设置自定义 Header，故 token 走 query param：
 * {@code ws://host/ws?token=<jwt>}。鉴权流程复刻 HTTP 的 JwtAuthInterceptor：
 * 校验 JWT → 解析 userId → 比对 Redis {@code token:<userId>}（支持踢人）→
 * 将 userId 写入 attributes 供 WebSocketHandler 读取。
 * <p>
 * 注意：此拦截器独立于 WebMvcConfig 的 /api/** HTTP 拦截器——/ws 不在其覆盖范围。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_TOKEN_PREFIX = "token:";
    private static final String ATTR_USER_ID = "userId";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request.getURI());
        if (token == null) {
            log.warn("WS 握手缺少 token");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        if (!jwtUtils.validateToken(token)) {
            log.warn("WS 握手 token 无效");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // Redis 比对：与 HTTP 拦截器保持一致，支持主动登出/踢人
        String redisKey = REDIS_TOKEN_PREFIX + userId;
        String cachedToken = (String) redisTemplate.opsForValue().get(redisKey);
        if (cachedToken == null || !cachedToken.equals(token)) {
            log.warn("WS 握手 token 已失效（Redis 不匹配）, userId={}", userId);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);

        attributes.put(ATTR_USER_ID, userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    /** 从 {@code ?token=xxx} 解析 token。 */
    private String extractToken(URI uri) {
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0 && "token".equals(pair.substring(0, idx))) {
                return pair.substring(idx + 1);
            }
        }
        return null;
    }
}