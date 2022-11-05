package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Integer> {
}
