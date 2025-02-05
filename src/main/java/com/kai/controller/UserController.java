package com.kai.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.kai.RedisOptEnum;
import com.kai.context.UserContext;
import com.kai.enums.MessageTypeEnum;
import com.kai.model.User;
import com.kai.model.req.EmailRequest;
import com.kai.model.req.LoginRequest;
import com.kai.model.req.RegisterRequest;
import com.kai.model.req.ResetPasswordRequest;
import com.kai.server.WebSocketMessageHandler;
import com.kai.service.EmailService;
import com.kai.service.RedisService;
import com.kai.service.UserService;
import com.kai.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.kai.common.R;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);



    @PostMapping("/register")
    public R<?> register(@RequestBody RegisterRequest registerRequest) {

        // 验证验证码
//        String cachedCaptcha = redisService.getVerificationCode(registerRequest.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);
//        if (cachedCaptcha == null || !cachedCaptcha.equals(registerRequest.getCaptchaCode())) {
//            return R.ok("Invalid or expired captcha code.");
//        }
//        //删除验证码
//        redisService.deleteVerificationCode(registerRequest.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);

        // 验证邮箱验证码
        String cachedCode = redisService.getVerificationCode(registerRequest.getEmail(), RedisOptEnum.EMAIL_VERIFICATION_CODES);
        if (cachedCode == null || !cachedCode.equals(registerRequest.getEmailCode())) {
            return R.fail("Invalid or expired email verification code.");
        }

        userService.registerUser(registerRequest);
        return R.ok("Registration successful.");
    }

    @PostMapping("/login")
    public R<?> loginUser(@RequestBody LoginRequest loginRequest) throws JsonProcessingException {

        // 验证验证码
        String cachedCaptcha = redisService.getVerificationCode(loginRequest.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);
        if (cachedCaptcha == null || !cachedCaptcha.equals(loginRequest.getCaptchaCode())) {
            return R.fail("Invalid or expired captcha code.");
        }
        //删除验证码
        redisService.deleteVerificationCode(loginRequest.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);

        Optional<User> optionalUser = userService.loginUser(loginRequest.getIdentifier(), loginRequest.getPassword());


        if(optionalUser.isPresent()){



            User loggedInUser = optionalUser.get();

            String hasToken = redisService.getRedisToken(String.valueOf(loggedInUser.getId()));
            if (hasToken != null) {
                redisService.delRedisToken(String.valueOf(loggedInUser.getId())); // 将旧会话踢下线

                WebSocketMessageHandler.broadcastPrivateMessage(loggedInUser.getUsername(), "当前账号在其他地方登录" , null, MessageTypeEnum.LOGOUT_MESSAGE,null);

            }


            String token = JwtUtil.generateToken(loggedInUser.getUsername(), String.valueOf(loggedInUser.getId()));

            redisService.setRedisToken(token, String.valueOf(loggedInUser.getId()));

            return R.ok(Map.of("token", token, "userId", loggedInUser.getId(),"username",loggedInUser.getUsername()));
        }else {
            logger.info("Login failed for user: 用户名或密码错误");

            return R.fail("Login failed");
        }


    }


    @GetMapping("/logout")
    public R<?> logout(){
        redisService.delRedisToken();
        return R.ok();
    }


    @PostMapping("/token/validate")
    public R<?> validateToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");
            // 假设 JwtUtil.validateToken 验证 token 并返回是否有效
            boolean validate = JwtUtil.validate(token);
            return R.ok(Map.of("valid", validate));
        } catch (Exception e) {
            return R.ok(Map.of("valid", false));
        }
    }

    @PostMapping("/register/send-email-code")
    public R<?> sendEmailCode(@RequestBody EmailRequest emailRequest) {


        String email = emailRequest.getEmail();
        //判断邮箱是否已经注册
        if (userService.isEmailRegistered(email)) {
            return R.fail("邮箱已注册");
        }

        // 生成验证码
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        try {
            redisService.saveVerificationCode(email, code, 5, RedisOptEnum.EMAIL_VERIFICATION_CODES);
        } catch (Exception e) {
            return R.fail("验证码已发送，请5分钟后再试");
        }
        // 发送邮件
        emailService.sendVerificationCode(email, code,"您正在注册账号，验证码有效期5分钟");
        return R.ok("Verification code sent successfully.");
    }



    @PostMapping("/reset-password/send-email-code")
    public R<?> resetPwd(@RequestBody EmailRequest emailRequest) {
        String email = emailRequest.getEmail();
        //判断邮箱是否已经注册
        if (!userService.isEmailRegistered(email)) {
            return R.fail("邮箱未注册");
        }

        // 生成验证码
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        try {
            redisService.saveVerificationCode(email, code, 5, RedisOptEnum.PASSWORD_RESET_CODES);
        } catch (Exception e) {
            return R.fail("验证码已发送，请5分钟后再试");
        }
        // 发送邮件
        emailService.sendVerificationCode(email, code,"您正在重置密码，验证码有效期5分钟");
        return R.ok("Verification code sent successfully.");
    }
    @PostMapping("/reset-password")
    public R<?> resetPassword(@RequestBody ResetPasswordRequest request) {

        // 验证验证码
//        String cachedCaptcha = redisService.getVerificationCode(request.getCaptchaKey(), RedisOptEnum.CAPTCHA_CODES);
//        if (cachedCaptcha == null || !cachedCaptcha.equals(request.getCaptchaCode())) {
//            return R.ok("Invalid or expired captcha code.");
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
            return R.fail("Invalid or expired email verification code.");
        }

        // 更新密码
        boolean isUpdated = userService.updatePasswordByEmail(email, newPassword);
        if (isUpdated) {
            // 删除 Redis 中的验证码
            redisService.deleteVerificationCode(email, RedisOptEnum.PASSWORD_RESET_CODES);
            return R.ok("密码重置成功！");
        } else {
            return R.fail("邮箱不存在，密码重置失败！");
        }
    }


}
