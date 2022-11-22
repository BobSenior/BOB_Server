package com.bob_senior.bob_server.domain.vote.entity;

import lombok.*;

import javax.persistence.*;

@Entity(name = "voteParticipated")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteParticipated {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteParticipantIdx;


    @Column
    private Long userIdx;

    @Column
    private Long voteRecordIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voteIdx")
    private Vote vote;

}
