package com.bob_senior.bob_server.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class BlockId implements Serializable {

    @Column
    private Long blockRequestUserIdx;

    @Column
    private Long blockUserIdx;
}
