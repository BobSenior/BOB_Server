package com.bob_senior.bob_server.domain.vote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MakeVoteDTO {

    private Integer makerIdx;

    private String title;

    private List<String> contents;

    private String voteType;

}