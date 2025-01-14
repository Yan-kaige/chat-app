package com.kai.controller;

import com.kai.context.UserContext;
import com.kai.model.ChatRoom;
import com.kai.model.InvitationMessage;
import com.kai.repository.ChatRoomRepository;
import com.kai.repository.InvitationMessageRepository;
import com.kai.service.ChatRoomService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class InvitationMessageController {

    private InvitationMessageRepository invitationMessageRepository;

    private SimpMessagingTemplate messagingTemplate;

    private ChatRoomRepository chatRoomRepository;


    @PostMapping("/chatroom/{roomId}/invite")
    public ResponseEntity<?> inviteUsers(
            @PathVariable Long roomId,
            @RequestBody List<Long> userIds) {
        Long senderId = UserContext.getUserId();
        ChatRoom chatRoomNotFound = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        List<InvitationMessage> invitationMessages = userIds.stream()
                .map(receiverId -> {
                    InvitationMessage message = new InvitationMessage();
                    message.setChatRoomId(roomId);
                    message.setSenderId(senderId);
                    message.setReceiverId(receiverId);
                    message.setMessageText("You have been invited to join Chat Room: " + roomId);
                    message.setExpiredAt(LocalDateTime.now().plusMinutes(5));
                    return message;
                }).collect(Collectors.toList());

        invitationMessageRepository.saveAll(invitationMessages);

        // 推送消息到被邀请者
        userIds.forEach(receiverId -> {
            messagingTemplate.convertAndSend("/topic/invite/" + receiverId, "收到一条邀请消息 邀请人："+ UserContext.getUsername() +"邀请你到聊天室: " + chatRoomNotFound.getName());
        });

        return ResponseEntity.ok("Invitations sent successfully");
    }


    @GetMapping("/user/invitations")
    public ResponseEntity<List<InvitationMessage>> getUserInvitations() {
        Long currentUserId = UserContext.getUserId();
        List<InvitationMessage> messages = invitationMessageRepository
                .findByReceiverIdAndStatus(currentUserId, "ACTIVE");
        return ResponseEntity.ok(messages);
    }


    @PostMapping("/invitations/{messageId}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long messageId) {
        InvitationMessage message = invitationMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        // 检查是否过期
        if (message.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invitation has expired");
        }

        // 标记消息为已读
        message.setStatus("READ");
        invitationMessageRepository.save(message);

        return ResponseEntity.ok("Invitation accepted");
    }


    //删除消息
    @DeleteMapping("/invitations/{messageId}")
    public ResponseEntity<?> deleteInvitation(@PathVariable Long messageId) {
        invitationMessageRepository.deleteById(messageId);
        return ResponseEntity.ok("Invitation deleted successfully");
    }


}
