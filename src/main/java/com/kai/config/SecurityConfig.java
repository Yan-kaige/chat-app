package com.kai.config;


import com.kai.service.RedisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(RedisService redisService) {
        return new JwtAuthenticationFilter(redisService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/token/validate","/api/register/**", "/api/login", "/ws/**", "/minio/**", "/api/file/**", "/api/reset-password/**", "/api/captcha/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .headers().frameOptions().disable() // 避免框架选项拦截
                .and()
                .addFilterBefore(jwtAuthenticationFilter(null), UsernamePasswordAuthenticationFilter.class); // 使用 Bean 注入
        return http.build();
    }
}

