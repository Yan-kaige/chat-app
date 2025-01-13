package com.kai.service;

import com.kai.context.UserContext;
import com.kai.model.PrivateMessage;
import com.kai.model.User;
import com.kai.repository.PrivateMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class PrivateMessageService {

    private final PrivateMessageRepository privateMessageRepository;

    private final SimpMessagingTemplate messagingTemplate;


    //获取两个用户的所有私聊消息
    public List<PrivateMessage> getPrivateMessages(Long friendId, Long chatRoomId) {
        Long userId= UserContext.getUserId();
        //分开两次查
        List<PrivateMessage> messages1 = privateMessageRepository.findAllBySenderIdAndReceiverIdAndChatRoomId(userId, friendId,chatRoomId);
        List<PrivateMessage> messages2 = privateMessageRepository.findAllBySenderIdAndReceiverIdAndChatRoomId(friendId, userId,chatRoomId);
        List<PrivateMessage> res=new ArrayList<>();

        //合并 时间排序  注意非空判断
        if(!CollectionUtils.isEmpty(messages1)){
            res.addAll(messages1);
        }
        if(!CollectionUtils.isEmpty(messages2)){
            res.addAll(messages2);
        }
        res.sort(Comparator.comparing(PrivateMessage::getCreatedAt));
        return res;

    }

    public void sendPrivateMessage(Long chatRoomId,Long receiverId, PrivateMessage privateMessage) {
        privateMessage.setSender(User.builder().id(UserContext.getUserId()).build());
        privateMessage.setChatRoomId(chatRoomId);
        privateMessage.setReceiver(User.builder().id(receiverId).build());
        privateMessageRepository.save(privateMessage);

        //通道唯一标识符构建  roomid   chatRoomId  receiverId
        String key = generateUniqueChannelIdentifier(chatRoomId,UserContext.getUserId(), receiverId);
        messagingTemplate.convertAndSend("/single/" +key , privateMessage);

    }

    public String generateUniqueChannelIdentifier(Long chatRoomId, Long senderId, Long receiverId) {
        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("Sender ID and Receiver ID cannot be null");
        }
        // 按升序排列，保证顺序不变
        long smallerId = Math.min(senderId, receiverId);
        long largerId = Math.max(senderId, receiverId);
        return "chatroom-"+chatRoomId+ "-"+ smallerId + "-" + largerId;
    }

}
