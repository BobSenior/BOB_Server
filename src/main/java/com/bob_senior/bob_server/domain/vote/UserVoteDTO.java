package com.bob_senior.bob_server.domain.vote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVoteDTO {

    private Long voteIdx;

    private Long userIdx;

    private Integer voteSelect;

}
