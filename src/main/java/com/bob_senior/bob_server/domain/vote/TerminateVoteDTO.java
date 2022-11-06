package com.bob_senior.bob_server.domain.vote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TerminateVoteDTO {

    private Integer terminatorIdx;
    private Integer voteIdx;

}
