package com.bob_senior.bob_server.domain.vote.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteParticipated {

    @Id
    private Long voteParticipatedIdx;


    @Column
    private Long userIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voteIdx")
    private Vote vote;

}
