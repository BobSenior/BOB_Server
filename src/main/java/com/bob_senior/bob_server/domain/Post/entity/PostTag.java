package com.bob_senior.bob_server.domain.Post.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postTagIdx;


    @Column(name = "tagContent")
    private String tagContent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "postIdx")
    private Post post;
}
