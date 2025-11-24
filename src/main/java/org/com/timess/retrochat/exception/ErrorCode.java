package org.com.timess.retrochat.exception;

import lombok.Getter;

/**
 * @author 33363
 */

@Getter
public enum ErrorCode {
    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    USER_NAME_ERROR(40001, "用户名不存在"),
    PASSWORD_ERROR(40002, "密码错误"),
    EMAIL_ERROR(40003, "邮箱已被注册"),
    VERIFY_CODE_ERROR(40004, "验证码错误或已过期"),

    NOT_LOGIN_ERROR(40100, "未登录"),
    NOT_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    TOO_MANY_REQUEST(42500, "请求过于频繁"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
