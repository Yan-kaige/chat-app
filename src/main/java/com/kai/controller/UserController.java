package com.kai.controller;


import com.kai.RedisOptEnum;
import com.kai.context.UserContext;
import com.kai.model.User;
import com.kai.model.req.EmailRequest;
import com.kai.model.req.LoginRequest;
import com.kai.model.req.RegisterRequest;
import com.kai.model.req.ResetPasswordRequest;
import com.kai.service.EmailService;
import com.kai.service.RedisService;
import com.kai.service.UserService;
import com.kai.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private EmailService emailService;



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {

        // 验证验证码
//        String cachedCaptcha = redisService.getVerificationCode(registerRequest.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);
//        if (cachedCaptcha == null || !cachedCaptcha.equals(registerRequest.getCaptchaCode())) {
//            return ResponseEntity.badRequest().body("Invalid or expired captcha code.");
//        }
//        //删除验证码
//        redisService.deleteVerificationCode(registerRequest.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);

        // 验证邮箱验证码
        String cachedCode = redisService.getVerificationCode(registerRequest.getEmail(), RedisOptEnum.EMAIL_VERIFICATION_CODES);
        if (cachedCode == null || !cachedCode.equals(registerRequest.getEmailCode())) {
            return ResponseEntity.badRequest().body("Invalid or expired email verification code.");
        }

        userService.registerUser(registerRequest);
        return ResponseEntity.ok("Registration successful.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {

        // 验证验证码
        String cachedCaptcha = redisService.getVerificationCode(loginRequest.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);
        if (cachedCaptcha == null || !cachedCaptcha.equals(loginRequest.getCaptchaCode())) {
            return ResponseEntity.badRequest().body("Invalid or expired captcha code.");
        }
        //删除验证码
        redisService.deleteVerificationCode(loginRequest.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);

        Optional<User> optionalUser = userService.loginUser(loginRequest.getIdentifier(), loginRequest.getPassword());

        if(optionalUser.isPresent()){
            User loggedInUser = optionalUser.get();
            String token = JwtUtil.generateToken(loggedInUser.getUsername(), String.valueOf(loggedInUser.getId()));
            return ResponseEntity.ok().body(Map.of("token", token, "userId", loggedInUser.getId()));
        }else {
            return ResponseEntity.badRequest().body("Login failed");
        }


    }

    @PostMapping("/token/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 假设 JwtUtil.validateToken 验证 token 并返回是否有效
            boolean validate = JwtUtil.validate(token);
            return ResponseEntity.ok(Map.of("valid", validate));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    @PostMapping("/register/send-email-code")
    public ResponseEntity<?> sendEmailCode(@RequestBody EmailRequest emailRequest) {


        String email = emailRequest.getEmail();
        //判断邮箱是否已经注册
        if (userService.isEmailRegistered(email)) {
            return ResponseEntity.badRequest().body("邮箱已注册");
        }

        // 生成验证码
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        try {
            redisService.saveVerificationCode(email, code, 5, RedisOptEnum.EMAIL_VERIFICATION_CODES);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("验证码已发送，请5分钟后再试");
        }
        // 发送邮件
        emailService.sendVerificationCode(email, code,"您正在注册账号，验证码有效期5分钟");
        return ResponseEntity.ok("Verification code sent successfully.");
    }



    @PostMapping("/reset-password/send-email-code")
    public ResponseEntity<?> resetPwd(@RequestBody EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        //判断邮箱是否已经注册
        if (!userService.isEmailRegistered(email)) {
            return ResponseEntity.badRequest().body("邮箱未注册");
        }

        // 生成验证码
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        try {
            redisService.saveVerificationCode(email, code, 5, RedisOptEnum.PASSWORD_RESET_CODES);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("验证码已发送，请5分钟后再试");
        }
        // 发送邮件
        emailService.sendVerificationCode(email, code,"您正在重置密码，验证码有效期5分钟");
        return ResponseEntity.ok("Verification code sent successfully.");
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

        // 验证验证码
//        String cachedCaptcha = redisService.getVerificationCode(request.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);
//        if (cachedCaptcha == null || !cachedCaptcha.equals(request.getCaptchaCode())) {
//            return ResponseEntity.badRequest().body("Invalid or expired captcha code.");
//        }
//
//        //删除验证码
//        redisService.deleteVerificationCode(request.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);

        String email = request.getEmail();
        String emailCode = request.getEmailCode();
        String newPassword = request.getNewPassword();

        // 验证邮箱验证码
        String cachedCode = redisService.getVerificationCode(email, RedisOptEnum.PASSWORD_RESET_CODES);
        if (cachedCode == null || !cachedCode.equals(emailCode)) {
            return ResponseEntity.badRequest().body("Invalid or expired email verification code.");
        }

        // 更新密码
        boolean isUpdated = userService.updatePasswordByEmail(email, newPassword);
        if (isUpdated) {
            // 删除 Redis 中的验证码
            redisService.deleteVerificationCode(email, RedisOptEnum.PASSWORD_RESET_CODES);
            return ResponseEntity.ok("密码重置成功！");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("邮箱不存在，密码重置失败！");
        }
    }


}
