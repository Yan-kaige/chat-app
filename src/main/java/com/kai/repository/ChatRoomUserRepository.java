package com.kai.repository;

import com.kai.model.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    List<ChatRoomUser> findAllByChatRoomId(Long roomId);

    boolean existsByUserIdAndChatRoomId(Long id, Long roomId);

    void deleteByChatRoomIdAndUserId(Long roomId, Long userId);
}
