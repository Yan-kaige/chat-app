package com.kai.model.req;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier; // 用户名/邮箱/账号
    private String password;
    private String captchaKey;
    private String captchaCode;
}