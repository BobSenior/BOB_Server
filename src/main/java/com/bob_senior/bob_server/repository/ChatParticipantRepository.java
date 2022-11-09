package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Chat.ChatParticipant;
import com.bob_senior.bob_server.domain.Chat.ChatNUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatNUser> {

    boolean existsChatParticipantById_ChatParticipantIdxAndId_ChatRoomIdx(Integer chatRoomIdx, Integer chatParticipantIdx);

    ChatParticipant getChatParticipantById_ChatParticipantIdxAndAndId_ChatRoomIdx(Integer chatRoomIdx,Integer chatParticipantIdx);

    @Query(value = "select cp.lastRead from ChatParticipant cp where cp.id.chatParticipantIdx = :participant")
    Timestamp getLastReadByUserIdx(@Param("participant") Integer participantIdx);

    ChatParticipant findChatParticipantById(ChatNUser rau);

    Long countChatParticipantById_ChatRoomIdx(Integer chatRoomIdx);

    Page<ChatParticipant> findAllById_ChatParticipantIdx(Integer chatParticipatedIdx, Pageable pageable);

}
