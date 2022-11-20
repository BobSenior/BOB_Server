package com.bob_senior.bob_server.domain.vote;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShownVoteRecord {

    private String content;

    private Integer count;
}
