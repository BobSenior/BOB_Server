package com.bob_senior.bob_server.configuration;

import com.bob_senior.bob_server.domain.Chat.entity.SessionRecord;
import com.bob_senior.bob_server.repository.ChatParticipantRepository;
import com.bob_senior.bob_server.repository.SessionRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.sql.Timestamp;

@Slf4j
@Configuration
public class DisconnectSocketConfig {

    private final SessionRecordRepository sessionRecordRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Autowired
    public DisconnectSocketConfig(SessionRecordRepository sessionRecordRepository, ChatParticipantRepository chatParticipantRepository) {
        this.sessionRecordRepository = sessionRecordRepository;
        this.chatParticipantRepository = chatParticipantRepository;
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event){
        //내가 알 수 있는건 오직 sessionId -> 이걸로 record안의 데이터를 가져옴
        SessionRecord sessionRecord = sessionRecordRepository.findBySessionId(event.getSessionId());
        System.out.println(sessionRecord);
        //저장된 세션을 지운다
        sessionRecordRepository.delete(sessionRecord);
        //할일 1 : 해당 유저의 lastRead갱신
        Long time = System.currentTimeMillis();
        Timestamp ts = new Timestamp(time);
        chatParticipantRepository.updateTimeStamp(ts,sessionRecord.getChatIdx(),sessionRecord.getUserIdx());


    }
}
