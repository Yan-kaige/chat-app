package com.kai.exception;


import com.kai.common.R;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获通用异常
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception ex) {
        ex.printStackTrace();
        return R.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器异常：" + ex.getMessage());
    }

    // 捕获自定义异常（可选）
    @ExceptionHandler(ServiceException.class)
    public R<?> handleCustomException(ServiceException ex) {
        return R.error(500, ex.getMessage());
    }
}
