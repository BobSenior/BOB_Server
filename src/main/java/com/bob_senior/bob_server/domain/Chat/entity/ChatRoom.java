package com.bob_senior.bob_server.domain.Chat.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ChatRoom {

    @Id
    private Integer chatRoomIdx;

    @Column(name = "chatRoomName")
    private String chatRoomName;

}
