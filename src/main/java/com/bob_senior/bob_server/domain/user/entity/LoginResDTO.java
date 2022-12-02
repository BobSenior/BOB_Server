package com.bob_senior.bob_server.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResDTO {
    private String resultMessage;

    private int userIdx;

    private String nickname;
    private String uuid;
    private String schoolId;
    private String department;
    private String imageURL;
    private String jwtAccessToken;
    private String jwtRefreshToken;

}
