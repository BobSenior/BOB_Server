package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostTagRepository extends JpaRepository<PostTag,Integer> {





}
