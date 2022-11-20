package com.bob_senior.bob_server.domain.appointment;

import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import com.bob_senior.bob_server.domain.user.UserProfile;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentHeadDTO {

    private Long postIdx;

    private String title;

    private Timestamp writtenAt;

    private String imageURL;

    private SimplifiedUserProfileDTO writer;

    private String location;

    private LocalDateTime meetingAt;

    private String type;

    private String status;

    private Integer totalNum;

    private Long currNum;

    private Long waitingNum;

}
