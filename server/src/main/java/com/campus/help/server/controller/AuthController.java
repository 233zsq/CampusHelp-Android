package com.campus.help.server.controller;

import com.campus.help.server.common.Result;
import com.campus.help.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器：登录 / 注册。
 * 替代 Android 端 LoginActivity 中的本地 Room 校验逻辑。
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    /** Redis 中缓存登录 token 的 key 前缀，须与 JwtAuthInterceptor 一致。 */
    private static final String REDIS_TOKEN_PREFIX = "token:";

    /**
     * 注册。
     * POST /api/auth/register
     * Body: { "studentId": "20210001", "password": "123456", "name": "小明" }
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        String password = body.get("password");
        String name = body.get("name");

        if (studentId == null || studentId.isBlank() || password == null || password.isBlank()) {
            return Result.fail(400, "学号和密码不能为空");
        }

        try {
            Long userId = userService.register(studentId, password, name);
            return Result.ok(userId);
        } catch (RuntimeException e) {
            return Result.fail(409, e.getMessage());
        }
    }

    /**
     * 登录。
     * POST /api/auth/login
     * Body: { "studentId": "20210001", "password": "123456" }
     * 返回: { "code": 0, "data": { "token": "eyJ...", "userId": 1 } }
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        String password = body.get("password");

        if (studentId == null || studentId.isBlank() || password == null || password.isBlank()) {
            return Result.fail(400, "学号和密码不能为空");
        }

        String token = userService.login(studentId, password);
        if (token == null) {
            return Result.fail(401, "学号或密码错误");
        }

        var user = userService.getByStudentId(studentId);
        return Result.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "name", user.getName(),
                "creditScore", user.getCreditScore()
        ));
    }

    /**
     * 登出。删除 Redis 中的 token 缓存，使旧 token 立即失效（踢人）。
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute("currentUserId") Long currentUserId) {
        redisTemplate.delete(REDIS_TOKEN_PREFIX + currentUserId);
        return Result.ok();
    }
}
