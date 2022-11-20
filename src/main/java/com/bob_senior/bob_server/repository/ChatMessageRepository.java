package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    Long countChatMessagesByChatRoom_ChatRoomIdxAndSentAtAfter(Long chatRoomIdx, Timestamp sentAt);

    @Transactional
    void deleteAllByChatRoom_ChatRoomIdx(long chatRoomIdx);
}
