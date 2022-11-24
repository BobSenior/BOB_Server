package com.bob_senior.bob_server.domain.appointment;

import com.bob_senior.bob_server.domain.Post.entity.PostParticipant;
import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import com.bob_senior.bob_server.domain.user.UserProfile;
import lombok.*;

import javax.persistence.OneToMany;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private List<String> tagHeads;

    @OneToMany(mappedBy = "")
    private List<PostParticipant> participantList = new ArrayList<>();

}
