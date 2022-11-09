package com.bob_senior.bob_server.domain.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SimplifiedUserProfileDTO {

    private String nickname;

    private String department;

    private String schoolId;

    private String school;

}
