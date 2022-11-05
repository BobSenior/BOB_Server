package com.bob_senior.bob_server.domain.vote;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VoteResult {

    private Integer voteIdx;

    private LocalDateTime createdAt;

    private String title;

    private List<VoteRecord> tuples;

    private Integer total_participated;


}
