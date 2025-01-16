package com.kai.model.req;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String emailCode;
    private String newPassword;
    private String captchaKey;
    private String captchaCode;

}
