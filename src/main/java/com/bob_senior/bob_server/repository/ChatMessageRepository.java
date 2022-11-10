package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Integer> {
    Long countChatMessagesByChatRoomIdxAndAndSentAtAfter(Integer chatRoomIdx, Timestamp sentAt);
}
