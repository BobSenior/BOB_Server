package com.bob_senior.bob_server.domain.Chat.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageIdx;

    @Column
    private Integer chatRoomIdx;

    @Column
    private Integer senderIdx;

    @Column
    private Timestamp sentAt;

    @Column
    private String msgContent;

    @Column
    private String uuId;


}
