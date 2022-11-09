package com.bob_senior.bob_server.domain.Chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SessionRecord {

    @Id
    private String sessionId;

    @Column
    private Integer userIdx;

    @Column
    private Integer chatIdx;
}
