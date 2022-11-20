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
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatParticipantIdx;

    @Embedded
    private ChatNUser chatNUser;


    @Column(length = 45)
    private String status;

    @Column
    private Timestamp lastRead;


}
