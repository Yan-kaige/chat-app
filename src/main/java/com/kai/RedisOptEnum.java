package com.kai;

import lombok.Data;

public enum RedisOptEnum {

    EMAIL_VERIFICATION_CODES("email_verification_codes", "邮箱验证码"),

    PASSWORD_RESET_CODES("password_reset_codes", "密码重置验证码"),

    CAPTCHA_CODES("captcha_codes", "图形验证码");

    private final String value;
    private final String desc;

    RedisOptEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

}
