package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ChatRepository extends JpaRepository<ChatMessage,Long> {
    Page<ChatMessage> findByChatRoom_ChatRoomIdx(Long chatRoomIdx, Pageable pageable);

    Long countBySentAtAfter(LocalDateTime lastRead);
}
