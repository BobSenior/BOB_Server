package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.email.entity.SchoolEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolEmailRepository extends JpaRepository<SchoolEmail,Long> {

    SchoolEmail findBySchoolName(String schoolName);
}
