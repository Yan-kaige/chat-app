package com.kai.service;


import com.kai.RedisOptEnum;
import com.kai.context.UserContext;
import com.kai.model.ChatRoom;
import com.kai.model.User;
import com.kai.model.req.RegisterRequest;
import com.kai.repository.ChatRoomRepository;
import com.kai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;

@Service
@AllArgsConstructor
public class UserService {
    private final ChatRoomRepository chatRoomRepository;
    private UserRepository userRepository;

    private ChatRoomUserService chatRoomUserService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    private RedisService redisService;

    public void registerUser(RegisterRequest registerRequest) {
        //检查用户名是否已存在
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        //检查邮箱是否已存在
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }


        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);
        redisService.deleteVerificationCode(registerRequest.getEmail(), RedisOptEnum.EMAIL_VERIFICATION_CODES);
    }

    public Optional<User> loginUser(String identifier, String password) {
        Optional<User> user = userRepository.findByUsernameOrEmail(identifier, identifier);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }
        return Optional.empty();
    }

    public List<User> getInvitableUsersForRoom(Long roomId) {
        // 获取已在聊天室的用户ID列表
        List<Long> joinedUserIds = chatRoomUserService.findUserIdsByChatRoomId(roomId);
        joinedUserIds.add(UserContext.getUserId());

        // 获取未加入聊天室的用户列表
        return userRepository.findUsersNotInIds(joinedUserIds);
    }

    public List<ChatRoom> getMyChatRooms() {
        Long userId = UserContext.getUserId();
        List<ChatRoom> chatRoomList = chatRoomRepository.findChatRoomsByCreatedBy(userId);

        if(CollectionUtils.isEmpty(chatRoomList)){
            return new ArrayList<>();
        }

        return  chatRoomList;
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean updatePasswordByEmail(String email, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
