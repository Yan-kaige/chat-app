package com.kai.controller;

import com.kai.model.ChatRoomMessage;
import com.kai.service.ChatRoomMessageService;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatRoomMessageControllerTest {

    @Autowired
    private ChatRoomMessageService chatRoomMessageService;

    @Test
    void getAllChatRoomsMsg() {
        List<ChatRoomMessage> allChatRoomMessagesByChatRoomId = chatRoomMessageService.getAllChatRoomMessagesByChatRoomId(2L);
        System.out.println(allChatRoomMessagesByChatRoomId);
    }
}