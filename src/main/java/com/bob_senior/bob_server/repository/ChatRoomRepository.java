package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
}
