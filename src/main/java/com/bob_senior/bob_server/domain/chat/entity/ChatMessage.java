package com.bob_senior.bob_server.domain.chat.entity;

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
    private Long messageIdx;

    @Column
    private Long senderIdx;

    @Column
    private Timestamp sentAt;

    @Column
    private String msgContent;

    @Column
    private String uuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoomIdx")
    private ChatRoom chatRoom;


}
