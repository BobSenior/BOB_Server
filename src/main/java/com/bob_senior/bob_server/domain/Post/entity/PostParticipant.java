package com.bob_senior.bob_server.domain.Post.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

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
}
