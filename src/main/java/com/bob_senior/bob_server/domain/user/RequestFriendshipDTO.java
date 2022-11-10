package com.bob_senior.bob_server.domain.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestFriendshipDTO {

    private Integer requesterIdx;

    private String targetUUID;

}
