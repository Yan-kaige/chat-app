package com.kai.util;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 自动生成安全的密钥
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1 天

    // 生成 JWT
    public static String generateToken(String username,String userId) {
        return Jwts.builder()
                .setSubject(username)
                .setId(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 验证 JWT
    public static Claims validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public static boolean validate(String token) {
        try {
            // 解析并验证 JWT
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // 设置签名密钥
                    .build()
                    .parseClaimsJws(token); // 如果签名验证失败，会抛出异常
            return true; // 如果解析成功，返回 true
        } catch (ExpiredJwtException e) {
            System.err.println("JWT 已过期: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("不支持的 JWT: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("JWT 格式错误: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("JWT 签名无效: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT 参数无效: " + e.getMessage());
        }
        return false; // 如果验证失败，返回 false
    }
}
