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
                    "where t.tag.tagContent like %:content% " +
                    "and t.post.recruitmentStatus = 'ACTIVATE'" +
                    "and (t.post.participantConstraint = 'ANY' or t.post.participantConstraint = :dep) "
    )
    Page<PostTag> searchTagThatCanParticipate(@Param("content") String content, @Param("dep") String dep, Pageable pageable);


    boolean existsByTag_TagContent(String tagContent);

    List<PostTag> findAllByTag_PostIdx(Integer postIdx);

}
