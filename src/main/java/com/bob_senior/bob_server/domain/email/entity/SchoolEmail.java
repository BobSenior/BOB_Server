package com.bob_senior.bob_server.domain.email.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.time.LocalDateTime;


@DynamicInsert
@Entity(name = "SchoolEmail")
@Table(name = "SchoolEmail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SchoolEmail{

    @Id
    private String schoolName;

    private String schoolEmail;

}