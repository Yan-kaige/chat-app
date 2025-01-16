package com.kai.model.req;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String emailCode;
    private String password;
    private String captchaKey;
    private String captchaCode;

}