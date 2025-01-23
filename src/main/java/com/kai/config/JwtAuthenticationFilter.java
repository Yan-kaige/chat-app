package com.kai.config;


import com.kai.RedisOptEnum;
import com.kai.context.UserContext;
import com.kai.exception.ServiceException;
import com.kai.service.RedisService;
import com.kai.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final RedisService redisService;

    @Autowired
    public JwtAuthenticationFilter(RedisService redisService) {
        this.redisService = redisService;
    }




    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // 获取请求路径
        String requestPath = request.getRequestURI();

        // 跳过不需要认证的路径
        if (requestPath.equals("/api/login") ||
                requestPath.equals("/api/register")
//                requestPath.startsWith("/ws")
                || requestPath.startsWith("/minio")
//                || requestPath.startsWith("/api/file")
                || requestPath.startsWith("/api/token/validate")
        ) { // 添加对 WebSocket 路径的放行
            filterChain.doFilter(request, response);
            return;
        }


        String authorizationHeader = request.getHeader("Authorization");
        auth(authorizationHeader);
        filterChain.doFilter(request, response);
    }

    private void auth(String authorizationHeader) throws ServletException {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {


                Claims claims = JwtUtil.validateToken(token);
                String username = claims.getSubject();
                String userId = claims.getId(); // 假设 token 中存储了 userId

                UserContext.setUserInfo(Long.valueOf(userId), username);
                UserDetails userDetails = User.withUsername(username).password("").authorities("USER").build();


                boolean existed = redisService.existKey(RedisOptEnum.LOGIN_INFO.getValue() + "_" + UserContext.getUserId());
                if(!existed){
                    throw new ServiceException("当前未登录！！！");
                }else {
                    String redisToken = redisService.getRedisToken(String.valueOf(UserContext.getUserId()));
                    if (!token.equals(redisToken)){
                        throw new ServletException("当前会话已经过期，请重新登录");
                    }
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                throw new ServletException(e.getMessage());
            }
        }
    }
}
