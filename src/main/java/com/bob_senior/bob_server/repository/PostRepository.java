package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;

public interface PostRepository extends JpaRepository<Post,Integer> {

    @Modifying
    @Query(value = "update Post p set p.meetingDate = :meetingDate where p.postIdx = :postIdx")
    void applyVoteResultDate(@Param("meetingDate")Timestamp meetingDate, @Param("postIdx") Integer postIdx);

    @Modifying
    @Query(value = "update Post p set p.place =:place where p.postIdx=:postIdx")
    void applyVoteResultLocation(@Param("place") String place, @Param("postIdx") Integer postIdx);

    @Modifying
    @Query(value = "update Post p set p.recruitmentStatus =:status where p.postIdx =:postIdx")
    void applyVoteResultRecruitment(@Param("status") boolean status, @Param("postIdx") Integer postIdx);

    Post findPostByPostIdx(Integer postIdx);

    boolean existsByPostIdxAndRecruitmentStatus(int postIdx, String status);

    @Query(value = "select p.participantLimit from Post p where p.postIdx =:postIdx")
    int getMaximumParticipationNumFromPost(@Param("postIdx") Integer postIdx);

    //activate이면서 + constraint가 ANY or input과 동일한 것들을 가져오기
    @Query(value = "select p from Post p where p.recruitmentStatus = 'ACTIVATE' and (p.participantConstraint = 'ANY' or p.participantConstraint = :dep)")
    Page<Post> getAllThatCanParticipant(@Param("dep") String dep);

    Page<Post> findAllByTitleLike(String title, Pageable pageable);

    @Query(value = "select p from Post p where p.recruitmentStatus = 'ACTIVATE' and (p.participantConstraint = 'ANY' or p.participantConstraint = :dep) and p.title like %:string%")
    Page<Post> searchAllParticipantThatCanParticipant(@Param("dep") String dep, @Param("string ") String string,Pageable pageable);
}
