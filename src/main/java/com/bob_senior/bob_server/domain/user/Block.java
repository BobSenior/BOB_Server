package com.bob_senior.bob_server.domain.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Block {


    @EmbeddedId
    private BlockId id;

}
