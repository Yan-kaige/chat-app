package com.kai.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    EmailService emailService;

//    @Test
//    void sendVerificationCode() {
//        emailService.sendVerificationCode("1339201475@qq.com","31312");
//    }
}