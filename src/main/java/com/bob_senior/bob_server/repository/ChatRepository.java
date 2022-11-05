package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Chat.ChatDto;
import com.bob_senior.bob_server.domain.Chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage,Integer> {
    Page<ChatMessage> findByChatRoomIdx(Integer chatRoomIdx, Pageable pageable);

    Long countBySentAtAfter(LocalDateTime lastRead);
}
