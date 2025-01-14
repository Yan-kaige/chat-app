package com.kai.controller;


import com.kai.context.UserContext;
import com.kai.model.User;
import com.kai.service.UserService;
import com.kai.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        Optional<User> optionalUser = userService.loginUser(user.getUsername(), user.getPassword());

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


}
