package com.microport.healthcert.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举类
 * 定义系统中所有可能的HTTP响应状态码
 * 
 * @author system
 * @date 2024
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 请求参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未授权（未登录或token无效）
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 禁止访问（无权限）
     */
    FORBIDDEN(403, "禁止访问，权限不足"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 服务器内部错误
     */
    SERVER_ERROR(500, "服务器内部错误");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态消息
     */
    private final String message;
}

