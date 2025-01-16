package com.kai.service;

import com.kai.RedisOptEnum;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    private final HashOperations<String, String, String> hashOperations;

    // Redis 键名


    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    /**
     * 保存邮箱验证码
     */
    public void saveVerificationCode(String email, String code, long timeoutMinutes, RedisOptEnum redisOptEnum) {
        // 检查邮箱是否已存在未过期的验证码
        if (hashOperations.hasKey(redisOptEnum.getValue(), email)) {
            throw new IllegalArgumentException("验证码已发送，请5分钟后再试");
        }

        hashOperations.put(redisOptEnum.getValue(), email, code);
        redisTemplate.expire(redisOptEnum.getValue(), timeoutMinutes, TimeUnit.MINUTES);
    }

    /**
     * 获取验证码
     */
    public String getVerificationCode(String email, RedisOptEnum redisOptEnum) {
        return hashOperations.get(redisOptEnum.getValue(), email);
    }

    /**
     * 删除验证码
     */
    public void deleteVerificationCode(String email, RedisOptEnum redisOptEnum) {
        hashOperations.delete(redisOptEnum.getValue(), email);
    }
}
