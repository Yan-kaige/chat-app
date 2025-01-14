package com.kai.repository;

import com.kai.model.InvitationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InvitationMessageRepository extends JpaRepository<InvitationMessage, Long> {

    // 查询用户的所有未过期的邀请消息
    List<InvitationMessage> findByReceiverIdAndStatus(Long receiverId, String status);

    // 查询特定聊天室的所有邀请消息
    List<InvitationMessage> findByChatRoomId(Long chatRoomId);

    // 删除特定消息
    void deleteById(Long id);

    // 删除过期的消息
    void deleteByExpiredAtBefore(LocalDateTime currentTime);
}

