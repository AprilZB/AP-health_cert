package com.microport.healthcert.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 用于封装所有API接口的返回数据，提供统一的响应格式
 * 
 * @author system
 * @date 2024
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private Long timestamp;

    /**
     * 私有构造函数，防止直接实例化
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     * 
     * @return Result对象
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功响应（带数据）
     * 
     * @param data 响应数据
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（带消息和数据）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @return Result对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 错误响应（使用默认错误码）
     * 
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SERVER_ERROR.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 错误响应（指定错误码和消息）
     * 
     * @param code 错误码
     * @param message 错误消息
     * @return Result对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 错误响应（使用ResultCode枚举）
     * 
     * @param resultCode 响应码枚举
     * @return Result对象
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }
}

