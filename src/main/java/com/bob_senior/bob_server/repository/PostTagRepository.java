package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag,Integer> {
}
