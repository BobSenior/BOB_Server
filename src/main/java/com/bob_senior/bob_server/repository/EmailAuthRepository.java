package com.bob_senior.bob_server.repository;


import com.bob_senior.bob_server.domain.email.entity.EmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;


public interface EmailAuthRepository extends JpaRepository<EmailAuth, Long> {

    @Transactional
    @Modifying
    @Query(value = "update EmailAuth ea set ea.expired = 'true' where ea.emailIdx = :emailIdx")
    void updateMailExpired(@Param("emailIdx") String emailIdx);



    EmailAuth findByEmailAndAndAuthToken(String email, String authToken);
}
