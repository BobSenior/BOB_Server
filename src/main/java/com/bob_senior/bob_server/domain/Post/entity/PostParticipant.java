package com.bob_senior.bob_server.domain.Post.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity(name="PostParticipant")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postParticipantIdx;

    @Column
    private Long userIdx;

    @Column(name="status")
    private String status;

    @Column
    private String position;

    /**
     * 연관관계
     */

    @ManyToOne
    @JoinColumn(name = "postIdx")
    private Post post;

}
