package com.kai.repository;

import com.kai.model.ChatRoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatRoomMessage, Long> {

    //通过创建时间排序
    List<ChatRoomMessage> findAllByChatRoomIdOrderByCreatedAt(Long chatRoomId);
}
