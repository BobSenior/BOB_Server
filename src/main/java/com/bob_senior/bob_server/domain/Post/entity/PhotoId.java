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
public class PhotoId implements Serializable {

    @Column(name = "postIdx")
    private Long postIdx;

    @Column(name = "postPhotoUrl")
    private String postPhotoUrl;

}
