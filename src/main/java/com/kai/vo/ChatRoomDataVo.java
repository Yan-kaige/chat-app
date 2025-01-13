package com.kai.vo;

import com.kai.model.ChatRoom;
import com.kai.model.ChatRoomMessage;
import lombok.Data;

import java.util.List;

@Data
public class ChatRoomDataVo {

    private ChatRoom chatRoom;

    private List<ChatRoomMessage> chatRoomMessageList;
}
