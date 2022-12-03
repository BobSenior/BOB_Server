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

    private Long makerIdx;

    private String title;

    private String location;

    private String latitude;

    private String longitude;

    private String time;

    private List<String> contents;

    private String voteType;

}
