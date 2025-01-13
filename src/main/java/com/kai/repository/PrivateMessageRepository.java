package com.kai.repository;

import com.kai.model.ChatRoom;
import com.kai.model.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivateMessageRepository  extends JpaRepository<PrivateMessage, Long> {

    List<PrivateMessage> findAllBySenderIdAndReceiverIdAndChatRoomId(Long userId, Long friendId,Long chatRoomId);
}
