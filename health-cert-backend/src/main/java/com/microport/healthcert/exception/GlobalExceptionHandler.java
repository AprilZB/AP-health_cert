package com.microport.healthcert.exception;

import com.microport.healthcert.common.Result;
import com.microport.healthcert.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理系统中所有未捕获的异常，返回统一的错误响应格式
 * 
 * @author system
 * @date 2024
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有Exception类型的异常
     * 
     * @param e 异常对象
     * @return 统一错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        // 记录异常日志
        log.error("系统异常：", e);
        
        // 返回统一错误响应
        return Result.error(ResultCode.SERVER_ERROR.getCode(), "系统异常：" + e.getMessage());
    }
}

