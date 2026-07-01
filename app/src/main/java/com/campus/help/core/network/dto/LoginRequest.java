package com.campus.help.core.network.dto;

/**
 * 登录请求体。字段名与后端 AuthController 期望的 { studentId, password } 一致。
 */
public class LoginRequest {

    public String studentId;
    public String password;

    public LoginRequest(String studentId, String password) {
        this.studentId = studentId;
        this.password = password;
    }
}
