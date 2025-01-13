package com.kai.repository;


import com.kai.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByName(String name); // 通过聊天室名称判断是否存在

}
