package com.bob_senior.bob_server.domain.user.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name="User")
@Getter
@Setter
@Table(name = "User")
public class User {

    @Id
    @Column(name = "userIdx")
    private Long userIdx;

    @Column(name = "userId")
    private String usrId;

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
