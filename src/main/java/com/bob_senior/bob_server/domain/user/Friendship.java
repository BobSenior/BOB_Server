package com.bob_senior.bob_server.domain.user;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Friendship {

    @EmbeddedId
    private FriendId id;

    @Column(name="status")
    private String status;

}
