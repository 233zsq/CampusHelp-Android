package com.campus.help.server.service;

import com.campus.help.server.entity.User;

/**
 * 用户 Service — 封装用户相关业务逻辑。
 */
public interface UserService {

    /**
     * 注册新用户。
     *
     * @param studentId 学号
     * @param password  明文密码（内部做 BCrypt 加密）
     * @param name      昵称
     * @return 新用户 ID
     */
    Long register(String studentId, String password, String name);

    /**
     * 登录验证。
     *
     * @param studentId 学号
     * @param password  明文密码
     * @return JWT token，验证失败返回 null
     */
    String login(String studentId, String password);

    /**
     * 根据 ID 查询用户。
     */
    User getById(Long id);

    /**
     * 根据学号查询用户。
     */
    User getByStudentId(String studentId);

    /**
     * 更新用户信息。
     */
    void update(User user);

    /**
     * 更新信用分。
     */
    void updateCreditScore(Long userId, int newScore);
}
