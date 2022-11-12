package com.bob_senior.bob_server.domain.Chat.entity;

import com.bob_senior.bob_server.domain.Chat.entity.ChatNUser;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer chatParticipantIdx;

    @Embedded
    private ChatNUser id;

    @Column(length = 45)
    private String status;

    @Column
    private Timestamp lastRead;


    @ManyToOne
    @JoinColumn(name = "chatRoomIdx")
    private ChatRoom chatRoom;


}
