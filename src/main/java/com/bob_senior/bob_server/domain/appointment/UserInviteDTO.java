package com.bob_senior.bob_server.domain.appointment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInviteDTO {

    private Long inviterIdx;

    private String invitedUUID;

    private String position;
}
