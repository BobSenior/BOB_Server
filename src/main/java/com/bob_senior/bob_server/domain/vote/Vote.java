package com.bob_senior.bob_server.domain.vote;

import lombok.*;

import javax.persistence.*;
import javax.websocket.OnOpen;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer voteIdx;

    @Column
    private Integer voteRoomIdx;

    @Column
    private String voteName;

    @Column
    private LocalDateTime createdAt;

    @Column
    private boolean isActivated;

    @Column
    private Integer participatedNum;

    @Column
    private Integer maxNum;

    @Column
    private String voteType;

    @Column
    private String UUID;
}
