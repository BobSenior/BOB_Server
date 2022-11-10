package com.bob_senior.bob_server.domain.user;

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
    private Integer minUserIdx;

    @Column(name = "maxUseridx")
    private Integer maxUserIdx;


    public FriendId(Integer user1, Integer user2) {
        this.minUserIdx = Math.min(user1,user2);
        this.maxUserIdx = Math.max(user1,user2);
    }
}
