package com.campus.help.core.network.dto;

/**
 * 注册请求体。name 可为空，后端默认用学号。
 */
public class RegisterRequest {

    public String studentId;
    public String password;
    public String name;

    public RegisterRequest(String studentId, String password, String name) {
        this.studentId = studentId;
        this.password = password;
        this.name = name;
    }
}
