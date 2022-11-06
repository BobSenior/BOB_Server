package com.bob_senior.bob_server.domain.Post;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity(name="PostParticipant")
public class PostParticipant {
    @EmbeddedId
    private PostAndUser postAndUser;

    @Column(name="status")
    private String status;
}
