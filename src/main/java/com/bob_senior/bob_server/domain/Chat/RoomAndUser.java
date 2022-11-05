package com.bob_senior.bob_server.domain.Chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class RoomAndUser implements Serializable {

    private Integer chatRoomIdx;
    private Integer chatParticipantIdx;

}
