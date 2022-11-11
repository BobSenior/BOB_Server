package com.bob_senior.bob_server.domain.user.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class User {

    @Id
    @Column(name = "userIdx")
    private Integer userIdx;

    @Column(name = "usrId")
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
