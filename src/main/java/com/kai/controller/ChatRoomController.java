package com.kai.controller;

import com.kai.model.*;
import com.kai.repository.ChatRoomUserRepository;
import com.kai.service.*;
import com.kai.util.AssertUtils;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import com.kai.common.R;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;


@RestController
@RequestMapping("/api/chatroom")
@AllArgsConstructor
public class ChatRoomController {

    private ChatRoomService chatRoomService;

    private ChatRoomUserService chatRoomUserService;

    private UserService userService;


    PrivateMessageService privateMessageService;

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomController.class);

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

//    // 发送消息
//    @PostMapping("/{roomId}/messages")
//    public R<ChatRoomMessage> sendMessage(@PathVariable Long roomId, @RequestBody ChatRoomMessage message) {
//        ChatRoomMessage chatRoomMessage = chatRoomService.sendMessage(roomId, message);
//        return R.ok(chatRoomMessage);
//    }

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

//    @PostMapping("/sendMediaToRoom/{roomId}")
//    public R<?> sendMediaToRoom(@RequestParam("file") MultipartFile file,@PathVariable Long roomId) {
//        try {
//
//
//
//            chatRoomService.sendMessage(file,roomId, chatRoomMessage);
//
//
//
//            // 返回音频 URL
//            return R.success("上传成功", Collections.singletonMap("url", url));
//        } catch (Exception e) {
//            logger.error("上传音频失败", e);
//            return R.error("上传失败");
//        }
//    }


//    @PostMapping("/sendMediaToPerson/{roomId}/{personId}")
//    public R<?> sendMediaToPerson(@RequestParam("file") MultipartFile file,@PathVariable("personId") Long personId,@PathVariable("roomId") Long roomId) {
//        try {
//
//
//            privateMessageService.sendPrivateMessage(file,roomId, personId,privateMessage);
//
//
//
//
//
//            // 返回音频 URL
//            return R.success("上传成功", Collections.singletonMap("url", url));
//        } catch (Exception e) {
//            logger.error("上传音频失败", e);
//            return R.error("上传失败");
//        }
//    }


}

