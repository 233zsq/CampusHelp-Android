package com.campus.help.server.controller;

import com.campus.help.server.common.Result;
import com.campus.help.server.entity.User;
import com.campus.help.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户控制器。
 * 对应 Android 端 UserRepository 的远端数据源。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息。
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        // 脱敏：不返回密码
        user.setPassword(null);
        return Result.ok(user);
    }

    /**
     * 更新用户信息（昵称、头像、手机号）。
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        if (body.containsKey("name")) {
            user.setName((String) body.get("name"));
        }
        if (body.containsKey("avatar")) {
            user.setAvatar((String) body.get("avatar"));
        }
        if (body.containsKey("phone")) {
            user.setPhone((String) body.get("phone"));
        }

        userService.update(user);
        return Result.ok();
    }
}
