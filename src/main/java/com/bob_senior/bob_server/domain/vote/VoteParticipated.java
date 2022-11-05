package com.bob_senior.bob_server.domain.vote;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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

}
