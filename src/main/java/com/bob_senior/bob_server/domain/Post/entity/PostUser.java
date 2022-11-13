package com.bob_senior.bob_server.domain.Post.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostUser implements Serializable {

    @Column(name = "postIdx")
    private Long postIdx;

    @Column(name = "userIdx")
    private Long userIdx;

}
