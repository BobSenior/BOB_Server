package com.bob_senior.bob_server.domain.email.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDateTime;


@DynamicInsert
@Entity(name = "EmailAuth")
@Table(name = "EmailAuth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EmailAuth {

    private static final Long MAX_EXPIRE_TIME = 20L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailIdx;

    private String email;
    private String authToken;
    private String expired;
    private LocalDateTime expireDate;

    @Builder
    public EmailAuth(String email, String authToken, String expired) {
        this.email = email;
        this.authToken = authToken;
        this.expired = expired;
        this.expireDate = LocalDateTime.now().plusMinutes(MAX_EXPIRE_TIME);
    }

    public void authMail(){
        this.expired = "true";
    }

}