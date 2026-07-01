package com.campus.help.server.interceptor;

import com.campus.help.server.common.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * JWT 认证拦截器（替代 Android 端的 TokenInterceptor）。
 * <p>
 * 校验流程：
 * 1. 从 Authorization 头提取 Bearer token
 * 2. 验证 JWT 签名和有效期
 * 3. 检查 Redis 中 token 是否存在（支持主动踢人）
 * 4. 将 userId 写入 request 属性，供 Controller 使用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_NAME = "Authorization";
    private static final String REDIS_TOKEN_PREFIX = "token:";
    private static final String ATTR_USER_ID = "currentUserId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // OPTIONS 请求放行（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(HEADER_NAME);
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            writeError(response, 401, "未登录，请先登录");
            return false;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());

        // 1. 校验 JWT
        if (!jwtUtils.validateToken(token)) {
            writeError(response, 401, "token 无效或已过期，请重新登录");
            return false;
        }

        // 2. 解析 userId
        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            writeError(response, 401, "token 无效");
            return false;
        }

        // 3. 检查 Redis 中是否存在（支持主动登出/踢人）
        String redisKey = REDIS_TOKEN_PREFIX + userId;
        String cachedToken = (String) redisTemplate.opsForValue().get(redisKey);
        if (cachedToken == null || !cachedToken.equals(token)) {
            writeError(response, 401, "token 已失效，请重新登录");
            return false;
        }

        // 4. 续期 Redis TTL
        redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);

        // 5. 将 userId 存入 request，Controller 通过 @RequestAttribute 获取
        request.setAttribute(ATTR_USER_ID, userId);
        return true;
    }

    private void writeError(HttpServletResponse response, int code, String message)
            throws Exception {
        response.setStatus(200); // 统一 200，用 body.code 区分
        response.setContentType("application/json;charset=UTF-8");
        // 用 HashMap 而非 Map.of：后者不接受 null 值，"data":null 会抛 NPE
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
