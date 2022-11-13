package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Chat.entity.ChatParticipant;
import com.bob_senior.bob_server.domain.Chat.entity.ChatNUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Integer> {

    boolean existsChatParticipantByChatNUser_UserIdxAndChatNUser_ChatRoomIdx(Integer chatRoomIdx, Integer chatParticipantIdx);

    ChatParticipant getChatParticipantByChatNUser_UserIdxAndChatNUser_ChatRoomIdx(Integer chatRoomIdx,Integer chatParticipantIdx);

    @Query(value = "select cp.lastRead from ChatParticipant cp where cp.chatNUser.userIdx = :participant")
    Timestamp getLastReadByUserIdx(@Param("participant") Integer participantIdx);

    ChatParticipant findChatParticipantByChatNUser(ChatNUser rau);

    Long countChatParticipantByChatNUser_ChatRoomIdx(Integer chatRoomIdx);

    @Modifying
    @Query(value = "update ChatParticipant cp set cp.lastRead = :timestamp, cp.status = 'Q' where cp.chatNUser.chatRoomIdx = :roomIdx and cp.chatNUser.userIdx = :userIdx")
    void updateTimeStamp(@Param("timestamp") Timestamp ts, @Param("roomIdx") Integer chatIdx, @Param("userIdx") Integer userIdx);

    @Modifying
    @Query(value = "update ChatParticipant cp set cp.status = 'A' where cp.chatNUser.userIdx = :userIdx and cp.chatNUser.chatRoomIdx = :roomIdx")
    void activateParticipation(@Param("userIdx") Integer userIdx, @Param("roomIdx") Integer roomIdx);

    @Query(value = "select cp from ChatParticipant cp where cp.status = 'Q' and cp.chatNUser.userIdx = :userIdx")
    List<ChatParticipant> getTotalUnreadChatNumber(@Param("userIdx") Integer userIdx);
}
