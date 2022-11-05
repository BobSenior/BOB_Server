package com.bob_senior.bob_server.domain.Post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostAndUser implements Serializable {

    private Integer postIdx;

    private Integer userIdx;

}
