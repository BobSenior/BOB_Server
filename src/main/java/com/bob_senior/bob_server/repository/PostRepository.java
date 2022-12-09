package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.Post;
import com.bob_senior.bob_server.domain.Post.entity.PostParticipant;
import org.apache.tomcat.jni.Local;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {

    @Transactional
    @Modifying
    @Query(value = "update Post p set p.meetingDate = :meetingDate, p.place = :place where p.postIdx = :postIdx")
    void applyVoteResultDateAndLocation(@Param("meetingDate") LocalDateTime meetingDate, @Param("place") String place, @Param("postIdx") Long postIdx);

    @Transactional
    @Modifying
    @Query(value = "update Post p set p.recruitmentStatus = 'fix' where p.postIdx=:postIdx")
    void fixPost(@Param("postIdx") long postIdx);



    @Transactional
    @Modifying
    @Query(value = "update Post p set p.place =:place where p.postIdx=:postIdx")
    void applyVoteResultLocation(@Param("place") String place, @Param("postIdx") Long postIdx);

    @Transactional
    @Modifying
    @Query(value = "update Post p set p.recruitmentStatus =:status where p.postIdx =:postIdx")
    void applyVoteResultRecruitment(@Param("status") String status, @Param("postIdx") Long postIdx);

    Post findPostByPostIdx(Long postIdx);

    boolean existsByPostIdxAndRecruitmentStatus(Long postIdx, String status);

    boolean existsByPostIdx(long postIdx);

    @Query(value = "select p.participantLimit from Post p where p.postIdx =:postIdx")
    int getMaximumParticipationNumFromPost(@Param("postIdx") Long postIdx);

    //activate이면서 + constraint가 ANY or input과 동일한 것들을 가져오기
    @Query(value = "select p from Post p " +
            "where p.recruitmentStatus = :status" +
            " and (p.participantConstraint = '아무나' or p.participantConstraint = :dep)" +
            " and (p.meetingDate > :time or p.meetingDate is null )" +
            " and not exists" +
            " (select pp from PostParticipant pp " +
            "where pp.post.postIdx = p.postIdx and pp.userIdx = :userIdx and pp.status='active')")
    Page<Post> getAllThatCanParticipant(@Param("status") String status, @Param("dep") String dep, @Param("time")LocalDateTime time,@Param("userIdx") long userIdx, Pageable pageable);

    @Query(value = "select p from Post p " +
            "where p.recruitmentStatus = 'active' " +
            "and (p.participantConstraint = '아무나' or p.participantConstraint = :dep) " +
            "and (p.meetingDate > :time or p.meetingDate is null) " +
            "and p.title LIKE %:title% " +
            "and not exists " +
            "(select pp from PostParticipant pp " +
            "where pp.post.postIdx = p.postIdx " +
            "and pp.userIdx = :userIdx " +
            "and pp.status='active')")
    Page<Post> getAllParticipantThatCanParticipant(@Param("dep") String dep, @Param("title") String title,@Param("time") LocalDateTime now,@Param("userIdx") long userIdx,Pageable pageable);


    @Query(value = "select p from Post p where not exists (select pp from PostParticipant pp where pp.post.postIdx = p.postIdx and pp.userIdx = 1)")
    List<Post> tete();

    Post findPostByWriterIdxAndAndChatRoomIdx(Long writerIdx, Long chatRoomIdx);



}
