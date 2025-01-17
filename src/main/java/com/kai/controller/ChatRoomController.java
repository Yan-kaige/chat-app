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
import com.kai.common.R;
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
    public R<String> createChatRoom(@RequestBody ChatRoom chatRoom) {
        String result = chatRoomService.createChatRoom(chatRoom);
        if (result.equals("Chatroom name already exists.")) {
            return R.ok(result);
        }
        return R.ok(result);
    }

    // 获取所有聊天室
    @GetMapping
    public R<List<ChatRoom>> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomService.getAllChatRooms();
        return R.ok(chatRooms);
    }

    // 删除聊天室
    @DeleteMapping("/{id}")
    public R<?> deleteChatRoom(@PathVariable Long id) {
        chatRoomService.deleteChatRoomById(id);
        return R.ok("Chat room deleted successfully");
    }


    // 获取单个聊天室
    @GetMapping("/{id}")
    public R<?> getChatRoom(@PathVariable Long id) {
        Optional<ChatRoom> chatRoomById = chatRoomService.getChatRoomById(id);
        if (chatRoomById.isEmpty()) {
            return R.ok("Chat room not found");
        }
        return R.ok(chatRoomById);
    }

    // 加入聊天室
    @PostMapping("/{roomId}/join")
    public R<?> joinChatRoom(@PathVariable Long roomId, @RequestParam(required = false) String password) {
        return chatRoomService.joinChatRoom(roomId, password);
    }

    // 发送消息
    @PostMapping("/{roomId}/messages")
    public R<ChatRoomMessage> sendMessage(@PathVariable Long roomId, @RequestBody ChatRoomMessage message) {
        ChatRoomMessage chatRoomMessage = chatRoomService.sendMessage(roomId, message);
        return R.ok(chatRoomMessage);
    }

    /**
     * 获取当前聊天室在线人员列表
     */
    @GetMapping("/{roomId}/online")
    public R<List<ChatRoomUser>> getOnlineUsers(@PathVariable Long roomId) {
        List<ChatRoomUser> userList = chatRoomUserService.findAllByChatRoomId(roomId);
        return R.ok(userList);
    }

    //退出聊天室
    @DeleteMapping("/{roomId}/exit")
    public R<String> exitChatRoom(@PathVariable Long roomId) {
        //根据用户id和聊天室id删除
        chatRoomUserService.exitChatRoom(roomId);
        return R.ok("Exit chatroom successfully");
    }


    // 获取聊天室可被邀请用户列表
    @GetMapping("/{roomId}/invite-list")
    public R<?> getInviteList(@PathVariable Long roomId) {
        try {
            List<User> inviteList = userService.getInvitableUsersForRoom(roomId);
            return R.ok(inviteList);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取可邀请用户列表失败：" + e.getMessage());
        }
    }

    // 获取我的聊天室
    @GetMapping("/my")
    public R<List<ChatRoom>> getMyChatRooms() {
        List<ChatRoom> myChatRooms = userService.getMyChatRooms();
        return R.ok(myChatRooms);
    }

    // 更新聊天室密码
    @PostMapping("/{roomId}/update-password")
    public R<?> updateChatRoomPassword(@PathVariable Long roomId, @RequestParam(value = "newPassword",required = false) String newPassword) {
        chatRoomService.updateChatRoomPassword(roomId, newPassword);
        return R.ok("Chat room password updated successfully");
    }




}

