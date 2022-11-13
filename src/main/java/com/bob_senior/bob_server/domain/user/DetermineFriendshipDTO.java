package com.bob_senior.bob_server.domain.user;

import lombok.Getter;

@Getter
public class DetermineFriendshipDTO {
    private Integer userIdx;

    private Integer targetIdx;

    private boolean accept;
}
