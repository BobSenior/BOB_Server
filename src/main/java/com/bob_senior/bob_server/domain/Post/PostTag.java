package com.bob_senior.bob_server.domain.Post;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PostTag {

    @Id
    private Integer tagContent;

    @Column
    private Integer postIdx;
}
