package com.bob_senior.bob_server.domain.vote;

import lombok.*;

import javax.persistence.*;
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
    private Integer postIdx;

    @Column
    private Integer creatorIdx;

    @Column
    private String title;

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
