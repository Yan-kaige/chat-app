package com.kai.server;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;


@Component
public class ChatRoomManager {
    // 存储房间和订阅用户的映射
    private final ConcurrentMap<String, CopyOnWriteArraySet<Channel>> chatRooms = new ConcurrentHashMap<>();

    /**
     * 订阅房间
     * @param roomId 房间 ID
     * @param channel 客户端连接通道
     */
    public void subscribe(String roomId, Channel channel) {
        // 如果房间不存在，初始化房间
        chatRooms.computeIfAbsent(roomId, key -> new CopyOnWriteArraySet<>());
        // 将客户端通道加入房间
        chatRooms.get(roomId).add(channel);
        System.out.println("Channel subscribed to room " + roomId);
    }

    /**
     * 取消订阅房间
     * @param roomId 房间 ID
     * @param channel 客户端连接通道
     */
    public void unsubscribe(String roomId, Channel channel) {
        CopyOnWriteArraySet<Channel> subscribers = chatRooms.get(roomId);
        if (subscribers != null) {
            // 从房间中移除客户端通道
            subscribers.remove(channel);
            System.out.println("Channel unsubscribed from room " + roomId);
            // 如果房间没有用户，删除房间
            if (subscribers.isEmpty()) {
                chatRooms.remove(roomId);
            }
        }
    }

    /**
     * 广播消息到房间
     * @param roomId 房间 ID
     * @param message 消息内容
     */
    public void broadcastMessage(String roomId, String message) {
        CopyOnWriteArraySet<Channel> subscribers = chatRooms.get(roomId);
        if (subscribers != null) {
            for (Channel channel : subscribers) {
                if (channel.isActive()) {
                    channel.writeAndFlush(message + "\n");
                }
            }
            System.out.println("Message broadcasted to room " + roomId + ": " + message);
        } else {
            System.out.println("Room " + roomId + " has no subscribers.");
        }
    }


}
