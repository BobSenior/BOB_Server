package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.PostPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostPhotoRepository extends JpaRepository<PostPhoto,Integer> {
}