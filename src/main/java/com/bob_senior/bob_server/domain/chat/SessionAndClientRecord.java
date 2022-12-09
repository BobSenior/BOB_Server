package com.bob_senior.bob_server.domain.chat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SessionAndClientRecord {

    private String sessionId;

    private Long userIdx;
}
