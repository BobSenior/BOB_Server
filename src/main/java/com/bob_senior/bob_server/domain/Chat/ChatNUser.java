package com.bob_senior.bob_server.domain.Chat;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatNUser implements Serializable {

    private Integer chatRoomIdx;
    private Integer chatParticipantIdx;

}
