package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsUserByUserIdx(Long userIdx);

    User findUserByUserIdx(Long userIdx);

    User findByUuid(String uuid);
}
