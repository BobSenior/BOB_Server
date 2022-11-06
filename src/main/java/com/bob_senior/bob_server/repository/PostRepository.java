package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Time;
import java.sql.Timestamp;

public interface PostRepository extends JpaRepository<Post,Integer> {

    @Query(value = "update Post p set p.meetingDate = :meetingDate where p.postIdx = :postIdx")
    void applyVoteResultDate(@Param("meetingDate")Timestamp meetingDate, @Param("postIdx") Integer postIdx);

    @Query(value = "update Post p set p.place =:place where p.postIdx=:postIdx")
    void applyVoteResultLocation(@Param("place") String place, @Param("postIdx") Integer postIdx);

    @Query(value = "update Post p set p.recruitmentStatus =:status where p.postIdx =:postIdx")
    void applyVoteResultRecruitment(@Param("status") boolean status, @Param("postIdx") Integer postIdx);

    Post findPostByPostIdx(Integer postIdx);


}
