package com.bob_senior.bob_server.domain.user;

import lombok.Getter;

@Getter
public class DetermineFriendshipDTO {
    private Long userIdx;

    private Long targetIdx;

    private boolean accept;
}
