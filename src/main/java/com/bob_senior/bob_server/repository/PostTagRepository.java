package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.Post;
import com.bob_senior.bob_server.domain.Post.entity.PostTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag,Long> {

    @Query(
            value = "select t " +
                    "from PostTag t join fetch t.post " +
                    "where t.tagContent like %:content% " +
                    "and t.post.recruitmentStatus = 'activate'" +
                    "and (t.post.participantConstraint = '아무나' or t.post.participantConstraint = :dep) "
    ,nativeQuery = true)
    Page<PostTag> searchTagThatCanParticipate(@Param("content") String content, @Param("dep") String dep, Pageable pageable);


    boolean existsByTagContent(String tagContent);

    List<PostTag> findAllByPost_PostIdx(Long postIdx);

    @Transactional
    void deleteAllByPost(Post post);


}
