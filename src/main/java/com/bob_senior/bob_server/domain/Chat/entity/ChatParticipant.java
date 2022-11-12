package com.bob_senior.bob_server.domain.Chat.entity;

import com.bob_senior.bob_server.domain.Chat.entity.ChatNUser;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer chatParticipantIdx;

    @Embedded
    private ChatNUser chatNUser;

    @Column(length = 45)
    private String status;

    @Column
    private Timestamp lastRead;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoomIdx")
    private ChatRoom chatRoom;


}
