package com.bob_senior.bob_server.domain.vote;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class VoteRecord {

    @EmbeddedId
    private VoteId voteId;

    @Column
    private String voteContent;

    @Column
    private Integer count;

    //참여한 user에 대한 정보를 어디서 보관해야되나....


}
