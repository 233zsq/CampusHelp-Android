package com.campus.help.core.network.dto;

/**
 * 登录成功响应 data（对应后端 AuthController.login 返回）。
 */
public class LoginResponse {

    public String token;
    public long userId;
    public String name;
    public int creditScore;
}
