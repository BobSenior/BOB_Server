package com.bob_senior.bob_server.domain.vote;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShownVoteHeadDTO {
    //카톡의 투표 기능을 보고 따옴

    private Long voteIdx;

    private String title;

    private Integer participatedNum;

    private boolean participated;
    //내가 참가했던 여부...
}
