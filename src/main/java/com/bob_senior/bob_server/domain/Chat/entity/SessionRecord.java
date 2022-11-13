package com.bob_senior.bob_server.domain.Chat.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "SessionRecord")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SessionRecord {

    @Id
    private String sessionId;

    @Column
    private Long userIdx;

    @Column
    private Long chatIdx;
}
