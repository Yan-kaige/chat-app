package com.kai.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Data
public class ChatRoomMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long messageId; // 消息 ID

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 所属聊天室

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 消息发送者

    @Column(name = "message", nullable = false)
    private String messageText; // 消息内容

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = " yyyy年MM月dd日 HH时mm分ss秒")
    private LocalDateTime createdAt; // 消息创建时间

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
