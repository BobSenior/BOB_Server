package com.bob_senior.bob_server.domain.appointment;

import com.bob_senior.bob_server.domain.user.UserProfile;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentViewDTO {

    private String location;

    private LocalDateTime meetingAt;

    private List<UserProfile> buyers;

    private List<UserProfile> receivers;

}
