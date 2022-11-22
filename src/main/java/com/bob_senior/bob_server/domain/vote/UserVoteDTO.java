package com.bob_senior.bob_server.domain.vote;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserVoteDTO {

    private Long voteIdx;

    private Long userIdx;

    private Integer voteSelect;

}
