package com.kai.service;


import com.kai.context.UserContext;
import com.kai.exception.ServiceException;
import com.kai.model.ChatRoom;
import com.kai.model.ChatRoomMessage;
import com.kai.model.ChatRoomUser;
import com.kai.model.User;
import com.kai.repository.ChatMessageRepository;
import com.kai.repository.ChatRoomRepository;
import com.kai.repository.ChatRoomUserRepository;
import com.kai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import com.kai.common.R;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatRoomService {

    private ChatRoomRepository chatRoomRepository;

    private UserRepository userRepository;

    private ChatRoomUserRepository chatRoomUserRepository;

    private ChatMessageRepository chatMessageRepository;

    private final SimpMessagingTemplate messagingTemplate;


    // 创建聊天室
    public String createChatRoom(ChatRoom chatRoom) {
        if (chatRoomRepository.existsByName(chatRoom.getName())) {
            return "Chatroom name already exists.";
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName(); // 获取当前登录用户名
        Assert.notNull(username, "Username must not be null.");
        userRepository.findByUsername(username).ifPresent(user -> {
            chatRoom.setCreatedByName(username);
            chatRoom.setCreatedBy(user.getId()); // 设置创建者

        });

        chatRoomRepository.save(chatRoom);
        return "Chatroom created successfully.";
    }

    // 获取所有聊天室
    public List<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    // 获取单个聊天室（可选，用于后续功能扩展）
    public Optional<ChatRoom> getChatRoomById(Long id) {
        return chatRoomRepository.findById(id);
    }

    public void deleteChatRoomById(Long id) {
        chatRoomRepository.deleteById(id);
    }


    public R<?> joinChatRoom(Long roomId,String password) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName(); // 获取当前登录用户名

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!chatRoomRepository.existsById(roomId)) {
            throw new RuntimeException("Chat room not found");
        }


        //先查此人是不是在这个聊天室里面
        if(chatRoomUserRepository.existsByUserIdAndChatRoomId(user.getId(),roomId)){
            return R.ok("Joined chatroom successfully");
        }

        //查询此聊天室是否有密码
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        //自己创建的聊天室不需要密码
        if(!chatRoom.getCreatedBy().equals(user.getId())){
            if(!StringUtils.isEmpty(chatRoom.getPassword()) && !chatRoom.getPassword().equals(password)){
                return R.fail("Password is incorrect");
            }
        }




        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setUser(User.builder().id(user.getId()).build());
        chatRoomUser.setChatRoom(ChatRoom.builder().id(roomId).build());
        chatRoomUser.setJoinedAt(LocalDateTime.now());
        chatRoomUserRepository.save(chatRoomUser);
        return R.ok("Joined chatroom successfully");
    }

    public ChatRoomMessage sendMessage(Long roomId, ChatRoomMessage message) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new RuntimeException("Chat room not found");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));


        message.setUser(user);
        message.setChatRoom(ChatRoom.builder().id(roomId).build());
        message.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(message);

        // 广播消息到 WebSocket 订阅的客户端
        messagingTemplate.convertAndSend("/topic/chatroom/" + roomId, message);

        // 广播消息到 WebSocket 订阅的客户端
        return message;
    }

    public void updateChatRoomPassword(Long roomId, String newPassword) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (!chatRoom.getCreatedBy().equals(UserContext.getUserId())) {
            throw new ServiceException("Only owner can update the password");
        }

        chatRoom.setPassword(newPassword);
        chatRoomRepository.save(chatRoom);
    }
}
