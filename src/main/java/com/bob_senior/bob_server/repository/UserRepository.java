package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsUserByUserIdx(Long userIdx);

    User findUserByUserIdx(Long userIdx);

    User findByUuid(String uuid);
    boolean existsByUuid(String uuid);
    User findTopByEmailOrderByCreatedAtDesc(String email);
    boolean existsByNickName(String nickname);
    boolean existsByUserId(String id);


    User findByUserIdAndPassword(String userId, String password);
    boolean existsByUserIdAndPassword(String userId, String password);
}
