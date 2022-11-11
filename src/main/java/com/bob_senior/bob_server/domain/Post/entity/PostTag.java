package com.bob_senior.bob_server.domain.Post.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class PostTag {

    @EmbeddedId
    private TagId id;

    @ManyToOne
    @JoinColumn(name = "postIdx")
    private Post post;
}
