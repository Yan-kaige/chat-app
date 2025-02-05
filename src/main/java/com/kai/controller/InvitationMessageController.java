package com.kai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kai.context.UserContext;
import com.kai.enums.MessageTypeEnum;
import com.kai.exception.ServiceException;
import com.kai.model.ChatRoom;
import com.kai.model.InvitationMessage;
import com.kai.model.User;
import com.kai.repository.ChatRoomRepository;
import com.kai.repository.InvitationMessageRepository;
import com.kai.repository.UserRepository;
import com.kai.server.WebSocketMessageHandler;
import com.kai.service.ChatRoomService;
import com.kai.util.SnowId;
import com.kai.vo.InvitationMessageVo;
import lombok.AllArgsConstructor;
import com.kai.common.R;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class InvitationMessageController {

    private InvitationMessageRepository invitationMessageRepository;


    private ChatRoomRepository chatRoomRepository;

    private UserRepository userRepository;


    WebSocketMessageHandler webSocketMessageHandler;


    @PostMapping("/chatroom/{roomId}/invite")
    public R<?> inviteUsers(
            @PathVariable Long roomId,
            @RequestBody List<Long> userIds) {
        Long senderId = UserContext.getUserId();
        ChatRoom chatRoomNotFound = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        List<InvitationMessage> invitationMessages = userIds.stream()
                .map(receiverId -> {
                    InvitationMessage message = new InvitationMessage();
                    long id = SnowId.nextId();
                    message.setId(id);
                    message.setChatRoomId(roomId);
                    message.setSenderId(senderId);
                    message.setReceiverId(receiverId);
                    message.setMessageText("You have been invited to join Chat Room: " + roomId);
                    message.setExpiredAt(LocalDateTime.now().plusMinutes(5));


                    userRepository.findById(receiverId).ifPresent(
                            user -> {
                                try {
                                    WebSocketMessageHandler.broadcastPrivateMessage(user.getUsername(), "You have been invited to join Chat Room: " + roomId, String.valueOf(id), MessageTypeEnum.NOTIFY_MESSAGE,null);
                                } catch (JsonProcessingException e) {
                                    throw new ServiceException("Failed to send invitation message");
                                }
                            }
                    );
                    return message;
                }).collect(Collectors.toList());

        invitationMessageRepository.saveAll(invitationMessages);



        return R.ok("Invitations sent successfully");
    }


    @GetMapping("/user/invitations")
    public R<List<InvitationMessageVo>> getUserInvitations() {
        Long currentUserId = UserContext.getUserId();
        List<InvitationMessage> messages = invitationMessageRepository
                .findByReceiverIdAndStatus(currentUserId, "ACTIVE");
        if(CollectionUtils.isEmpty(messages)){
            return R.ok(new ArrayList<>());
        }

        //toVo返回
        return R.ok(messages.stream().map(InvitationMessage::toVo).collect(Collectors.toList()));
    }


    @PostMapping("/invitations/{messageId}/accept")
    public R<?> acceptInvitation(@PathVariable Long messageId) {
        InvitationMessage message = invitationMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        // 检查是否过期
        if (message.getExpiredAt().isBefore(LocalDateTime.now())) {
            return R.fail("Invitation has expired");
        }

        // 标记消息为已读
        message.setStatus("READ");
        invitationMessageRepository.save(message);

        return R.ok("Invitation accepted");
    }


    //删除消息
    @DeleteMapping("/invitations/{messageId}")
    public R<?> deleteInvitation(@PathVariable Long messageId) {
        invitationMessageRepository.deleteById(messageId);
        return R.ok("Invitation deleted successfully");
    }


}
