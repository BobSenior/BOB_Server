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
    private Integer voteIdx;

    @Column
    private Integer userIdx;

    @ManyToOne
    @JoinColumn(name = "voteIdx")
    private Vote vote;

}
