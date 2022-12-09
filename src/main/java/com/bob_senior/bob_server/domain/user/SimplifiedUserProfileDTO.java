package com.bob_senior.bob_server.domain.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimplifiedUserProfileDTO {
    //TODO : 데이터 추가

    private Long userIdx;

    private String nickname;

    private String department;

    private String schoolId; //

    private String school;

    private String uuid;

    private boolean isOnline;

    //private String uuid;

    //private String profileImg;
}
