package com.bob_senior.bob_server.domain.Chat;

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

    @EmbeddedId
    private RoomAndUser id;

    @Column(length = 45)
    private String status;

    @Column
    private Timestamp lastRead;



}
