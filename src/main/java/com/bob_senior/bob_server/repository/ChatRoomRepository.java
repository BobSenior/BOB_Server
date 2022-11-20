package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    ChatRoom findChatRoomByChatRoomName(String name);

}
