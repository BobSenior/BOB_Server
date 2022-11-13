package com.bob_senior.bob_server.domain.vote.entity;

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
    private Long voteIdx;

    @Column
    private Long postIdx;

    @Column
    private Long creatorIdx;

    @Column
    private String title;

    @Column
    private LocalDateTime createdAt;

    @Column
    private String isActivated;

    @Column
    private Integer participatedNum;

    @Column
    private Integer maxNum;

    @Column
    private String voteType;

    @Column
    private String UUID;
}
