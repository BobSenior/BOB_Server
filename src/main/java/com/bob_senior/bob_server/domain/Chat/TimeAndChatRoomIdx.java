package com.bob_senior.bob_server.domain.Chat;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class TimeAndChatRoomIdx {

    private Integer chatRoomIdx;

    private Timestamp timestamp;

}
