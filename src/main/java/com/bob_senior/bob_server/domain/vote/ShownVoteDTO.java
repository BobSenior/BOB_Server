package com.bob_senior.bob_server.domain.vote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ShownVoteDTO {

    private Vote vote;
    private List<VoteRecord> records;

}
