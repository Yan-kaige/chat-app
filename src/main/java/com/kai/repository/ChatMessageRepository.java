package com.kai.repository;

import com.kai.model.ChatRoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatRoomMessage, Long> {

    List<ChatRoomMessage> findAllByChatRoomId(Long chatRoomId);
}
