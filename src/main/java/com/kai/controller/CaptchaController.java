package com.kai.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.kai.RedisOptEnum;
import com.kai.common.R;
import com.kai.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CaptchaController {

    @Autowired
    private DefaultKaptcha captchaProducer;

    @Autowired
    private RedisService redisService;

    @Value("${captcha.expiration:300}")
    private long captchaExpiration;

    @GetMapping("/captcha/create")
    public R<?> generateCaptcha() {
        // 生成验证码文本和图片
        String text = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(text);

        // 使用 UUID 作为验证码唯一标识
        String captchaKey = UUID.randomUUID().toString();

        // 保存到 Redis，设置过期时间
        redisService.saveVerificationCode(captchaKey, text, captchaExpiration, RedisOptEnum.CAPTCHA_CODES);

        // 转为 Base64 字符串
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
            String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());
            Map<String, String> response = Map.of(
                    "key", captchaKey,
                    "image", base64Image
            );
            return R.ok(response);
        } catch (IOException e) {
            return R.fail("生成验证码失败！");
        }
    }


}
