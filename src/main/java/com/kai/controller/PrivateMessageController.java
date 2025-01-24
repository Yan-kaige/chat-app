package com.kai.controller;

import com.kai.model.PrivateMessage;
import com.kai.service.PrivateMessageService;
import lombok.AllArgsConstructor;
import com.kai.common.R;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/privateMsg")
@AllArgsConstructor
public class PrivateMessageController {


    private PrivateMessageService privateMessageService;

    // 获取两个用户的所有私聊消息
    @GetMapping("/{chatRoomId}/{friendId}")
    public R<List<PrivateMessage>> getPrivateMessages(@PathVariable Long chatRoomId, @PathVariable Long friendId) {
        List<PrivateMessage> messages = privateMessageService.getPrivateMessages( friendId,chatRoomId);
        return R.ok(messages);
    }

    //私聊消息发送
//    @PostMapping("/send/{chatRoomId}/{friendId}")
//    public R<?> sendPrivateMessage(@PathVariable Long chatRoomId, @PathVariable Long friendId, @RequestBody PrivateMessage privateMessage) {
//        privateMessageService.sendPrivateMessage(chatRoomId,friendId,privateMessage);
//        return R.ok();
//    }

}
