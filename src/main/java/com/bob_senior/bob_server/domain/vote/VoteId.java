package com.bob_senior.bob_server.domain.vote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VoteId implements Serializable {

    //어떤 vote에 대응하는지
    private Integer voteIdx;

    //몇번째 선택지인지
    private Integer choiceIdx;
}
