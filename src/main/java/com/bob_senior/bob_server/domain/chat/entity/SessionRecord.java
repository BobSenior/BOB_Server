package com.bob_senior.bob_server.domain.chat.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "SessionRecord")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SessionRecord {

    @Id
    private String sessionId;

    @Column
    private Long userIdx;

    @Column
    private Long chatIdx;
}
