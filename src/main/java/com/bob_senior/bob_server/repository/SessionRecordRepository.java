package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.Chat.SessionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.websocket.Session;

public interface SessionRecordRepository extends JpaRepository<SessionRecord,String> {
    SessionRecord findBySessionId(String sessionId);
}
