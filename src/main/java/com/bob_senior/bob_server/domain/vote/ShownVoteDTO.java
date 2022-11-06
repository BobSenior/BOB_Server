package com.bob_senior.bob_server.domain.vote;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShownVoteDTO {

    private Integer voteIdx;

    private Integer writerIdx;

    private String nickname;

    private LocalDateTime createdAt;

    private String title;

    private Integer totalParticipated;

    private List<VoteRecord> records;

}
