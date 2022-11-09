package com.bob_senior.bob_server.domain.appointment;

import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import com.bob_senior.bob_server.domain.user.UserProfile;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentHeadDTO {

    private String title;

    private String imageUrl;

    private SimplifiedUserProfileDTO writer;

    private String location;

    private LocalDateTime meetingAt;

    private String type;

    private String status;

}
