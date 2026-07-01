package com.campus.help.server.common;

import lombok.Getter;

/**
 * 统一错误码枚举。
 * 与 Android 端 ApiResponse.code 对应：0 = 成功，非 0 = 失败。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或 token 已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
