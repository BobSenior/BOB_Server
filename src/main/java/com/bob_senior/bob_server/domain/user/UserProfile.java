package com.bob_senior.bob_server.domain.user;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfile {

    private String nickname;

    private String schoolId;

    private boolean isOnline;

    private String profileImgURL;
    //gravatar용 string으로 대체 가능

}
