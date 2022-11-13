package com.bob_senior.bob_server.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@Getter
public class FriendId implements Serializable {

    @Column(name = "minUserIdx")
    private Long minUserIdx;

    @Column(name = "maxUseridx")
    private Long maxUserIdx;


    public FriendId(Long user1, Long user2) {
        this.minUserIdx = Math.min(user1,user2);
        this.maxUserIdx = Math.max(user1,user2);
    }
}
