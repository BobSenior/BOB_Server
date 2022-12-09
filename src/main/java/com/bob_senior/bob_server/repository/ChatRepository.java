package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public interface ChatRepository extends JpaRepository<ChatMessage,Long> {
    Page<ChatMessage> findByChatRoom_ChatRoomIdx(Long chatRoomIdx, Pageable pageable);

    Page<ChatMessage> findByChatRoom_ChatRoomIdxOrderBySentAtDesc(Long chatRoomIdx, Pageable pageable);

    Long countChatMessagesByChatRoom_ChatRoomIdxAndSentAtIsAfter(long chatroomIdx, Timestamp lastRead);

    Long countByChatRoomChatRoomIdx(Long chatroomIdx);

    @Transactional
    void deleteAllByChatRoom_ChatRoomIdx(long chatroomIdx);
}
