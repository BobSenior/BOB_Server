package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.Post;
import com.bob_senior.bob_server.domain.Post.entity.PostParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostParticipantRepository extends JpaRepository<PostParticipant, Long> {

    PostParticipant findByPost_PostIdxAndUserIdxAndStatus(long postIdx, long userIdx, String status);

    List<PostParticipant> findPostParticipantsByPost_PostIdxAndStatusAndPosition(Long postIdx, String status, String position);

    boolean existsByPost_PostIdxAndUserIdxAndStatus(long postIdx, long userIdx, String status);

    Long countByPost_PostIdxAndStatus(Long postIdx, String status);

    Page<PostParticipant> findAllByPost_PostIdxAndStatus(Long postIdx, String Status, Pageable pageable);

    @Query(value = "select p.userIdx from PostParticipant p where p.status='active' and p.post.postIdx=:postIdx")
    List<Long> getAllUserIdxInPostActivated(@Param("postIdx") Long postIdx);

    @Transactional
    @Modifying
    @Query(value = "update PostParticipant p set p.status = :status where p.post.postIdx =:postIdx and p.userIdx = :userIdx")
    void changePostParticipationStatus(@Param("status") String status, @Param("postIdx") Long postIdx, @Param("userIdx") Long userIdx);


    boolean existsByPost_PostIdxAndUserIdx(long postIdx, long userIdx);

    Page<PostParticipant> findAllByUserIdxAndStatus(Long userIdx, String Status, Pageable pageable);


    @Transactional
    void deleteByPost_PostIdxAndUserIdx(long postIdx, long userIdx);



    @Transactional
    @Modifying
    @Query(value = "delete from PostParticipant p where p.post.postIdx = :postIdx")
    void deleteAllParticipantInPost(Long postIdx);

    Long countByPost_PostIdxAndStatusAndPosition(Long postIdx, String status, String position);
}
