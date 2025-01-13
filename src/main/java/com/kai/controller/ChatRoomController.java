package com.kai.controller;

import com.kai.model.ChatRoom;
import com.kai.model.ChatRoomMessage;
import com.kai.model.User;
import com.kai.repository.UserRepository;
import com.kai.service.ChatRoomService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/chatroom")
@AllArgsConstructor
public class ChatRoomController {

    private ChatRoomService chatRoomService;

    // 创建聊天室
    @PostMapping("/create")
    public ResponseEntity<String> createChatRoom(@RequestBody ChatRoom chatRoom) {
        String result = chatRoomService.createChatRoom(chatRoom);
        if (result.equals("Chatroom name already exists.")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // 获取所有聊天室
    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomService.getAllChatRooms();
        return ResponseEntity.ok(chatRooms);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChatRoom(@PathVariable Long id) {
        chatRoomService.deleteChatRoomById(id);
        return ResponseEntity.ok("Chat room deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChatRoom(@PathVariable Long id) {
        Optional<ChatRoom> chatRoomById = chatRoomService.getChatRoomById(id);
        if (chatRoomById.isEmpty()) {
            return ResponseEntity.badRequest().body("Chat room not found");
        }
        return ResponseEntity.ok(chatRoomById);
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<String> joinChatRoom(@PathVariable Long roomId) {
        chatRoomService.joinChatRoom(roomId);
        return ResponseEntity.ok("Joined chatroom successfully");
    }

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatRoomMessage> sendMessage(@PathVariable Long roomId, @RequestBody ChatRoomMessage message) {
        ChatRoomMessage chatRoomMessage = chatRoomService.sendMessage(roomId, message);
        return ResponseEntity.ok(chatRoomMessage);
    }

}

