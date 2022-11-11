package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Chat.ChatParticipant;
import com.bob_senior.bob_server.domain.Chat.ChatNUser;
import com.fasterxml.jackson.databind.ser.std.TimeZoneSerializer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatNUser> {

    boolean existsChatParticipantById_ChatParticipantIdxAndId_ChatRoomIdx(Integer chatRoomIdx, Integer chatParticipantIdx);

    ChatParticipant getChatParticipantById_ChatParticipantIdxAndId_ChatRoomIdx(Integer chatRoomIdx,Integer chatParticipantIdx);

    @Query(value = "select cp.lastRead from ChatParticipant cp where cp.id.chatParticipantIdx = :participant")
    Timestamp getLastReadByUserIdx(@Param("participant") Integer participantIdx);

    ChatParticipant findChatParticipantById(ChatNUser rau);

    Long countChatParticipantById_ChatRoomIdx(Integer chatRoomIdx);

    Page<ChatParticipant> findAllById_ChatParticipantIdx(Integer chatParticipatedIdx, Pageable pageable);

    @Modifying
    @Query(value = "update ChatParticipant cp set cp.lastRead = :timestamp, cp.status = 'Q' where cp.id.chatRoomIdx = :roomIdx and cp.id.chatParticipantIdx = :userIdx")
    void updateTimeStamp(@Param("timestamp") Timestamp ts, @Param("roomIdx") Integer chatIdx, @Param("userIdx") Integer userIdx);

    @Modifying
    @Query(value = "update ChatParticipant cp set cp.status = 'A' where cp.id.chatParticipantIdx = :userIdx and cp.id.chatRoomIdx = :roomIdx")
    void activateParticipation(@Param("userIdx") Integer userIdx, @Param("roomIdx") Integer roomIdx);

    @Query(value = "select cp from ChatParticipant cp where cp.status = 'Q' and cp.id.chatParticipantIdx = :userIdx")
    List<ChatParticipant> getTotalUnreadChatNumber(@Param("userIdx") Integer userIdx);
}
