package com.bob_senior.bob_server.domain.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KickUserDTO {

    private long kickerIdx;

    private long kickedIdx;

}
