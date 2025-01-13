package com.kai.service;

import com.kai.model.ChatRoomMessage;
import com.kai.repository.ChatMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatRoomMessageService {

    private ChatMessageRepository chatMessageRepository;

    // 获取所有聊天信息根据聊天室id
    public List<ChatRoomMessage> getAllChatRoomMessagesByChatRoomId(Long chatRoomId) {
        return chatMessageRepository.findAllByChatRoomId(chatRoomId);
    }
}
