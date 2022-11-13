package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.PostTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag,Integer> {

    @Query(
            value = "select t " +
                    "from PostTag t join fetch t.post " +
                    "where t.tagContent like %:content% " +
                    "and t.post.recruitmentStatus = 'ACTIVATE'" +
                    "and (t.post.participantConstraint = 'ANY' or t.post.participantConstraint = :dep) "
    ,nativeQuery = true)
    Page<PostTag> searchTagThatCanParticipate(@Param("content") String content, @Param("dep") String dep, Pageable pageable);


    boolean existsByTagContent(String tagContent);

    List<PostTag> findAllByPost_PostIdx(Integer postIdx);


}
