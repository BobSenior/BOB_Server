package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Post.PostUser;
import com.bob_senior.bob_server.domain.appointment.AppointmentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, PostUser> {

    Page<AppointmentRequest> findAllByPostAndUser_UserIdxAndStatus(Integer userIdx, String status, Pageable pageable);

}