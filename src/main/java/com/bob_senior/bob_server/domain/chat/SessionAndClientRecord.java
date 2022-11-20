package com.bob_senior.bob_server.domain.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionAndClientRecord {

    private String sessionId;

    private Long userIdx;
}
