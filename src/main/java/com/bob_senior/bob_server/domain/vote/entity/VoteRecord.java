package com.bob_senior.bob_server.domain.vote.entity;

import com.bob_senior.bob_server.domain.vote.entity.VoteId;
import lombok.*;

import javax.persistence.*;

@Entity(name = "voteRecord")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class VoteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteRecordIdx;

    @Embedded
    private VoteId voteId;

    @Column(name = "content")
    private String voteContent;

    @Column
    private Integer count;

}
