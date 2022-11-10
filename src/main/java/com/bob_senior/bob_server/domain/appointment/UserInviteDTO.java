package com.bob_senior.bob_server.domain.appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInviteDTO {

    private Integer inviterIdx;

    private String invitedUUID;
}
