package com.bob_senior.bob_server.domain.user.entity;

import com.bob_senior.bob_server.domain.user.entity.FriendId;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer friendshipIdx;


    @Embedded
    private FriendId friendInfo;

    @Column(name="status")
    private String status;

}
