package com.kai.service;

import com.kai.context.UserContext;
import com.kai.exception.ServiceException;
import com.kai.model.PrivateMessage;
import com.kai.model.User;
import com.kai.repository.PrivateMessageRepository;
import com.kai.repository.UserRepository;
import com.kai.server.WebSocketMessageHandler;
import com.kai.util.SnowId;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@AllArgsConstructor
public class PrivateMessageService {

    private final PrivateMessageRepository privateMessageRepository;

//    private final SimpMessagingTemplate messagingTemplate;


    private final UserRepository userRepository;

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
        sendPrivateMessage(privateMessage);

        //通道唯一标识符构建  roomid   chatRoomId  receiverId
//        String key = generateUniqueChannelIdentifier(chatRoomId,UserContext.getUserId(), receiverId);
//        messagingTemplate.convertAndSend("/single/" +key , privateMessage);

    }
    public String sendPrivateMessage(PrivateMessage privateMessage) {
        privateMessage.setSender(User.builder().id(UserContext.getUserId()).build());
        //主键生成
        //主键id为空 则设置
        if(privateMessage.getId()==null){
            privateMessage.setId(SnowId.nextId());
        }

        privateMessageRepository.save(privateMessage);

        Optional<User> receiver = userRepository.findById(privateMessage.getReceiver().getId());
        if (receiver.isEmpty()) {
            throw new ServiceException("Receiver not found.");
        }


        //私聊消息广播

        if(!"1".equals(privateMessage.getMessageType())){
            WebSocketMessageHandler.broadcastPrivateMessage(String.valueOf(privateMessage.getChatRoomId()),receiver.get().getUsername(),String.valueOf(privateMessage.getId()));
        }


        return receiver.get().getUsername();

        //通道唯一标识符构建  roomid   chatRoomId  receiverId
//        String key = generateUniqueChannelIdentifier(chatRoomId,UserContext.getUserId(), receiverId);
//        messagingTemplate.convertAndSend("/single/" +key , privateMessage);

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
