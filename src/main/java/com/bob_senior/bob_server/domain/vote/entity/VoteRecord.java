package com.bob_senior.bob_server.domain.vote.entity;

import com.bob_senior.bob_server.domain.vote.entity.VoteId;
import lombok.*;

import javax.persistence.*;

@Entity
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

    @Column
    private String voteContent;

    @Column
    private Integer count;

}
