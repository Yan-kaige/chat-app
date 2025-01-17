package com.kai.common;


import lombok.Data;

@Data
public class R<T> {
    private Integer code;  // 状态码
    private String message; // 消息
    private T data; // 数据

    // 成功静态方法
    public static <T> R<T> success() {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("Success");
        r.setData(null);
        return r;
    }

    // 成功静态方法
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("Success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> success(String message, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }


    // 成功静态方法
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("Success");
        r.setData(null);
        return r;
    }

    // 成功静态方法
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("Success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> ok(String message, T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    // 失败静态方法
    public static <T> R<T> error(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(null);
        return r;
    }

    public static <T> R<T> error(String message) {
        return error(500, message);
    }

    // 失败静态方法
    public static <T> R<T> fail(Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(null);
        return r;
    }

    public static <T> R<T> fail(String message) {
        return error(500, message);
    }
}
