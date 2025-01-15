package com.kai.controller;

import com.kai.model.ChatRoom;
import com.kai.model.ChatRoomMessage;
import com.kai.model.ChatRoomUser;
import com.kai.model.User;
import com.kai.repository.ChatRoomUserRepository;
import com.kai.service.ChatRoomService;
import com.kai.service.ChatRoomUserService;
import com.kai.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/chatroom")
@AllArgsConstructor
public class ChatRoomController {

    private ChatRoomService chatRoomService;

    private ChatRoomUserService chatRoomUserService;

    private UserService userService;

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

    // 删除聊天室
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChatRoom(@PathVariable Long id) {
        chatRoomService.deleteChatRoomById(id);
        return ResponseEntity.ok("Chat room deleted successfully");
    }


    // 获取单个聊天室
    @GetMapping("/{id}")
    public ResponseEntity<?> getChatRoom(@PathVariable Long id) {
        Optional<ChatRoom> chatRoomById = chatRoomService.getChatRoomById(id);
        if (chatRoomById.isEmpty()) {
            return ResponseEntity.badRequest().body("Chat room not found");
        }
        return ResponseEntity.ok(chatRoomById);
    }

    // 加入聊天室
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinChatRoom(@PathVariable Long roomId, @RequestParam(required = false) String password) {
        return chatRoomService.joinChatRoom(roomId, password);
    }

    // 发送消息
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatRoomMessage> sendMessage(@PathVariable Long roomId, @RequestBody ChatRoomMessage message) {
        ChatRoomMessage chatRoomMessage = chatRoomService.sendMessage(roomId, message);
        return ResponseEntity.ok(chatRoomMessage);
    }

    /**
     * 获取当前聊天室在线人员列表
     */
    @GetMapping("/{roomId}/online")
    public ResponseEntity<List<ChatRoomUser>> getOnlineUsers(@PathVariable Long roomId) {
        List<ChatRoomUser> userList = chatRoomUserService.findAllByChatRoomId(roomId);
        return ResponseEntity.ok(userList);
    }

    //退出聊天室
    @DeleteMapping("/{roomId}/exit")
    public ResponseEntity<String> exitChatRoom(@PathVariable Long roomId) {
        //根据用户id和聊天室id删除
        chatRoomUserService.exitChatRoom(roomId);
        return ResponseEntity.ok("Exit chatroom successfully");
    }


    // 获取聊天室可被邀请用户列表
    @GetMapping("/{roomId}/invite-list")
    public ResponseEntity<?> getInviteList(@PathVariable Long roomId) {
        try {
            List<User> inviteList = userService.getInvitableUsersForRoom(roomId);
            return ResponseEntity.ok(inviteList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("获取可邀请用户列表失败：" + e.getMessage());
        }
    }

    // 获取我的聊天室
    @GetMapping("/my")
    public ResponseEntity<List<ChatRoom>> getMyChatRooms() {
        List<ChatRoom> myChatRooms = userService.getMyChatRooms();
        return ResponseEntity.ok(myChatRooms);
    }

    // 更新聊天室密码
    @PostMapping("/{roomId}/update-password")
    public ResponseEntity<?> updateChatRoomPassword(@PathVariable Long roomId, @RequestParam(value = "newPassword",required = false) String newPassword) {
        chatRoomService.updateChatRoomPassword(roomId, newPassword);
        return ResponseEntity.ok("Chat room password updated successfully");
    }




}

