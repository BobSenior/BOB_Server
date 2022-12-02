package com.bob_senior.bob_server.domain.user.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.TypeAlias;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name="User")
@Getter
@Setter
@Table(name = "User")
@DynamicInsert
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "userIdx")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userIdx;

    @Column(name = "userId")
    private String userId;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column
    private Timestamp createdAt;

    @Column
    private Timestamp updatedAt;

    @Column(name="status")
    private String status;

    @Column(name="nickName")
    private String nickName;

    @Column(name="authorizedStatus")
    private String authorizedStatus;

    @Column(name = "uuid")
    private String uuid;

    @Column
    private String schoolId;

    @Column
    private String imageURL;

    @Column
    private String department;

    @Column
    private String school;

}
