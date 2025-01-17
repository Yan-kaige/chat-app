package com.kai.controller;

import com.kai.model.ChatRoomMessage;
import com.kai.service.ChatRoomMessageService;
import lombok.AllArgsConstructor;
import com.kai.common.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chatroomMsg")
@AllArgsConstructor
public class ChatRoomMessageController {

    private ChatRoomMessageService chatRoomMessageService;


    // 获取所有聊天室信息
    @GetMapping("/{roomId}")
    public R<List<ChatRoomMessage>> getAllChatRoomsMsg(@PathVariable Long roomId) {
        List<ChatRoomMessage> msg = chatRoomMessageService.getAllChatRoomMessagesByChatRoomId(roomId);
        return R.ok(msg);
    }

}
