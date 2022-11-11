package com.bob_senior.bob_server.domain.Post.entity;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class PostPhoto {

    @EmbeddedId
    private PhotoId id;

    @ManyToOne
    @JoinColumn(name = "postIdx")
    private Post post;

}
