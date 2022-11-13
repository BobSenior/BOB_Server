package com.bob_senior.bob_server.domain.Post.entity;

import lombok.Cleanup;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
public class TagId implements Serializable {

    @Column(name = "tagContent")
    private String tagContent;


    @Column(name = "postIdx")
    private Integer postIdx;

}
