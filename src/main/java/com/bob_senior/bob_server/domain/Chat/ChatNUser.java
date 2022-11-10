package com.bob_senior.bob_server.domain.Chat;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatNUser implements Serializable {

    @Column(name = "chatRoomIdx")
    private Integer chatRoomIdx;
    @Column(name = "chatParticipantIdx")
    private Integer chatParticipantIdx;

}
