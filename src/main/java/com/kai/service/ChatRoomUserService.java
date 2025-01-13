package com.kai.service;

import com.kai.context.UserContext;
import com.kai.model.ChatRoomUser;
import com.kai.repository.ChatRoomUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@AllArgsConstructor
public class ChatRoomUserService {

    private ChatRoomUserRepository chatRoomUserRepository;

    public List<ChatRoomUser> findAllByChatRoomId(Long roomId) {
        return chatRoomUserRepository.findAllByChatRoomId(roomId);
    }


    @Transactional
    public void exitChatRoom(Long roomId) {
        //根据当前登录人
        Long userId = UserContext.getUserId();
        chatRoomUserRepository.deleteByChatRoomIdAndUserId(roomId, userId);

    }
}
