package com.campus.help.core.network;

/**
 * 统一后端响应壳：{ "code": 0, "message": "...", "data": ... }
 * code == 0 视为成功；具体 code 定义由后端约定。
 */
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return code == 0;
    }
}
