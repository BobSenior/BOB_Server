package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.chat.entity.ChatParticipant;
import com.bob_senior.bob_server.domain.chat.entity.ChatNUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    boolean existsChatParticipantByChatNUser_UserIdxAndChatNUser_ChatRoomIdx(Long userIdx, Long chatRoomIdx);

    boolean existsByStatus(String status);

    boolean existsByChatNUser_UserIdxAndChatNUser_ChatRoomIdx(Long userIdx, Long chatRoomIdx);

    ChatParticipant getByChatNUser_ChatRoomIdxAndChatNUser_UserIdx(Long chatRoomIdx, Long userIdx);

    List<ChatParticipant> getAllByChatNUser_UserIdx(Long userIdx);

    @Query(value = "select cp.lastRead from ChatParticipant cp where cp.chatNUser.userIdx = :participant and cp.chatNUser.chatRoomIdx = :chatroomIdx")
    Timestamp getLastReadByUserIdx(@Param("participant") Long participantIdx,@Param("chatroomIdx") Long chatroomIdx);

    ChatParticipant findChatParticipantByChatNUser(ChatNUser rau);

    @Transactional
    @Modifying
    @Query(value = "update ChatParticipant cp set cp.lastRead = :timestamp where cp.chatNUser.chatRoomIdx = :roomIdx and cp.chatNUser.userIdx = :userIdx")
    void updateTimeStamp(@Param("timestamp") Timestamp ts, @Param("roomIdx") Long chatIdx, @Param("userIdx") Long userIdx);

    @Transactional
    @Modifying
    @Query(value = "update ChatParticipant cp set cp.status = 'A' where cp.chatNUser.userIdx = :userIdx and cp.chatNUser.chatRoomIdx = :roomIdx")
    void activateParticipation(@Param("userIdx") Long userIdx, @Param("roomIdx") Long roomIdx);

    @Query(value = "select cp from ChatParticipant cp where cp.status = 'Q' and cp.chatNUser.userIdx = :userIdx")
    List<ChatParticipant> getTotalUnreadChatNumber(@Param("userIdx") Long userIdx);

    @Transactional
    void deleteByChatNUser(ChatNUser chatNUser);

    @Transactional
    @Modifying
    @Query(value = "delete from ChatParticipant cp where cp.chatNUser.chatRoomIdx = :roomIdx")
    void deleteAllParticipationInChatroom(@Param("roomIdx") Long roomIdx);

    @Transactional
    void deleteByChatNUser_UserIdxAndChatNUser_ChatRoomIdx(long userIdx, long chatRoomIdx);
}
