package com.bob_senior.bob_server.domain.vote.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VoteId implements Serializable {

    @Column(name = "voteIdx")
    //어떤 vote에 대응하는지
    private Long voteIdx;

    @Column(name = "choice")
    //몇번째 선택지인지
    private Integer choiceIdx;

}
