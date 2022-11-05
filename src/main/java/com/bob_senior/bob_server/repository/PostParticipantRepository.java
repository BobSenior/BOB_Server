package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.PostAndUser;
import com.bob_senior.bob_server.domain.Post.PostParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostParticipantRepository extends JpaRepository<PostParticipant, PostAndUser> {
}
