package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.PostUser;
import com.bob_senior.bob_server.domain.Post.entity.PostParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostParticipantRepository extends JpaRepository<PostParticipant, PostUser> {

    List<PostParticipant> findPostParticipantsById_PostIdxAndStatus(Integer postIdx, String status);

    boolean existsById(PostUser id);

    Long countById_PostIdxAndStatus(int postIdx, String status);

    Page<PostParticipant> findAllById_PostIdxAndStatus(Integer postIdx, String Status, Pageable pageable);

    @Modifying
    @Query(value = "update PostParticipant p set p.status = :status where p.id.postIdx =:postIdx and p.id.userIdx = :userIdx")
    void changePostParticipationStatus(@Param("status") String status, @Param("postIdx") Integer postIdx, @Param("userIdx") Integer userIdx);

    boolean existsByIdAndAndStatus(PostUser id, String status);



}
