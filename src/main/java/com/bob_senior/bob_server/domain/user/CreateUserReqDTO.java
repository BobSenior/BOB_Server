package com.bob_senior.bob_server.domain.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateUserReqDTO {
    private String email;
    private String userId;
    private String password;

    private String school;
    private String schoolId;
    private String nickName;
    private String department;

}
