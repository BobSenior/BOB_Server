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
    @EmbeddedId
    private PostUser id;

    @Column(name="status")
    private String status;

    /**
     * 연관관계
     */

    @ManyToOne
    @JoinColumn(name = "postIdx")
    private Post post;
}
