package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.Post;
import com.bob_senior.bob_server.domain.Post.entity.PostPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PostPhotoRepository extends JpaRepository<PostPhoto, Long> {


    PostPhoto findPostPhotoByPost_PostIdx(Long postIdx);

    @Transactional
    void deleteAllByPost(Post post);


}
