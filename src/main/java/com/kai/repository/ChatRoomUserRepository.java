package com.kai.repository;

import com.kai.model.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

}
