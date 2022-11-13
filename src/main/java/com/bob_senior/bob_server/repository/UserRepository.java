package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
    boolean existsUserByUserIdx(Integer userIdx);

    User findUserByUserIdx(Integer userIdx);

    User findByUuid(String uuid);
}
