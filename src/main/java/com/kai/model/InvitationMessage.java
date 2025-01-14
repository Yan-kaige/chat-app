package com.kai.model;


import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitation_message")
@Data
public class InvitationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 主键 ID

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId; // 聊天室 ID

    @Column(name = "sender_id", nullable = false)
    private Long senderId; // 发送者 ID

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId; // 接收者 ID

    @Column(name = "message_text", nullable = false)
    private String messageText; // 消息内容

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // 消息状态：ACTIVE/EXPIRED/READ/DELETED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 创建时间

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt; // 过期时间

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

