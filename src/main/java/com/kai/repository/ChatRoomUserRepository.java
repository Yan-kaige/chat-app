package com.kai.repository;

import com.kai.model.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    List<ChatRoomUser> findAllByChatRoomId(Long roomId);

    boolean existsByUserIdAndChatRoomId(Long id, Long roomId);

    void deleteByChatRoomIdAndUserId(Long roomId, Long userId);

    @Query("SELECT cu.user.id FROM ChatRoomUser cu WHERE cu.chatRoom.id = :roomId")
    List<Long> findUserIdsByChatRoomId(@Param("roomId") Long roomId);
}
