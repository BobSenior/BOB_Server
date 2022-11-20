package com.bob_senior.bob_server.domain.Post.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tagIdx")
    private Long postTagIdx;


    @Column(name = "tagContent")
    private String tagContent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "postIdx")
    private Post post;
}
