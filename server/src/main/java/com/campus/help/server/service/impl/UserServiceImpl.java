package com.campus.help.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.help.server.common.JwtUtils;
import com.campus.help.server.entity.User;
import com.campus.help.server.mapper.UserMapper;
import com.campus.help.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    /** BCrypt 密码编码器（Spring Security 提供，无需额外引入 spring-security-web） */
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private static final String REDIS_TOKEN_PREFIX = "token:";

    @Override
    public Long register(String studentId, String password, String name) {
        // 检查学号是否已注册
        User existing = getByStudentId(studentId);
        if (existing != null) {
            throw new RuntimeException("学号已注册");
        }

        User user = new User();
        user.setStudentId(studentId);
        user.setPassword(PASSWORD_ENCODER.encode(password));
        user.setName(name != null ? name : studentId);
        user.setCreditScore(600); // 默认信用分
        user.setCreatedAt(System.currentTimeMillis());
        userMapper.insert(user);

        log.info("用户注册成功: studentId={}, id={}", studentId, user.getId());
        return user.getId();
    }

    @Override
    public String login(String studentId, String password) {
        User user = getByStudentId(studentId);
        if (user == null) {
            return null;
        }

        if (!PASSWORD_ENCODER.matches(password, user.getPassword())) {
            return null;
        }

        // 生成 JWT
        String token = jwtUtils.generateToken(user.getId(), user.getStudentId());

        // 存入 Redis（7 天过期）
        String redisKey = REDIS_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(redisKey, token, 7, TimeUnit.DAYS);

        log.info("用户登录成功: studentId={}, id={}", studentId, user.getId());
        return token;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByStudentId(String studentId) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getStudentId, studentId)
        );
    }

    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }

    @Override
    public void updateCreditScore(Long userId, int newScore) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setCreditScore(newScore);
            userMapper.updateById(user);
        }
    }
}
