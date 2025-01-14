package com.kai.config;


import com.kai.context.UserContext;
import com.kai.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // 获取请求路径
        String requestPath = request.getRequestURI();

        // 跳过不需要认证的路径
        if (requestPath.equals("/api/login") ||
                requestPath.equals("/api/register") ||
                requestPath.startsWith("/ws")
                || requestPath.startsWith("/minio")
                || requestPath.startsWith("/api/file")
        ) { // 添加对 WebSocket 路径的放行
            filterChain.doFilter(request, response);
            return;
        }


        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                Claims claims = JwtUtil.validateToken(token);
                String username = claims.getSubject();
                String userId = claims.getId(); // 假设 token 中存储了 userId

                UserContext.setUserInfo(Long.valueOf(userId), username);
                UserDetails userDetails = User.withUsername(username).password("").authorities("USER").build();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
