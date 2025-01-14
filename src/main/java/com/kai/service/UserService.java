package com.kai.service;


import com.kai.context.UserContext;
import com.kai.model.User;
import com.kai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;

    private ChatRoomUserService chatRoomUserService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
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
}
