package com.bob_senior.bob_server.domain.vote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVoteDTO {

    private Integer voteIdx;

    private Integer userIdx;

    private Integer voteSelect;

}
