package com.kai.service;


import com.kai.model.ChatRoom;
import com.kai.model.ChatRoomMessage;
import com.kai.model.ChatRoomUser;
import com.kai.model.User;
import com.kai.repository.ChatMessageRepository;
import com.kai.repository.ChatRoomRepository;
import com.kai.repository.ChatRoomUserRepository;
import com.kai.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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


    public void joinChatRoom(Long roomId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName(); // 获取当前登录用户名

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!chatRoomRepository.existsById(roomId)) {
            throw new RuntimeException("Chat room not found");
        }

        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setUser(User.builder().id(user.getId()).build());
        chatRoomUser.setChatRoom(ChatRoom.builder().id(roomId).build());
        chatRoomUser.setJoinedAt(LocalDateTime.now());
        chatRoomUserRepository.save(chatRoomUser);
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
}
