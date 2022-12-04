package com.bob_senior.bob_server.domain.appointment;

import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestData {
    private SimplifiedUserProfileDTO simp;

    private String position;

}
