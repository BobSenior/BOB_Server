package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.PostUser;
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

    List<PostParticipant> findPostParticipantsByPostUser_PostIdxAndStatusAndPosition(Long postIdx, String status,String position);

    boolean existsByPostUser(PostUser id);

    Long countByPostUser_PostIdxAndStatus(Long postIdx, String status);

    Page<PostParticipant> findAllByPostUser_PostIdxAndStatus(Long postIdx, String Status, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "update PostParticipant p set p.status = :status where p.postUser.postIdx =:postIdx and p.postUser.userIdx = :userIdx")
    void changePostParticipationStatus(@Param("status") String status, @Param("postIdx") Long postIdx, @Param("userIdx") Long userIdx);

    boolean existsByPostUserAndAndStatus(PostUser id, String status);

    Page<PostParticipant> findAllByPostUser_UserIdxAndStatus(Long userIdx, String Status, Pageable pageable);


    @Transactional
    void deleteByPostUser(PostUser postUser);


    @Transactional
    @Modifying
    @Query(value = "delete from PostParticipant p where p.postUser.postIdx = :postIdx")
    void deleteAllParticipantInPost(Long postIdx);

    Long countByPostUser_PostIdxAndStatusAndPosition(Long postIdx, String status, String position);
}
