package com.bob_senior.bob_server.domain.appointment;

import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import com.bob_senior.bob_server.domain.user.UserProfile;
import com.bob_senior.bob_server.domain.vote.ShownVoteRecord;
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

    private Long postIdx;

    private Long writerIdx;

    private String title;

    private String location;

    private String latitude;

    private String longitude;

    private String constraint;

    private String type;

    private String meetingAt;

    private List<SimplifiedUserProfileDTO> buyers;

    private Integer maxBuyerNum;

    private List<SimplifiedUserProfileDTO> receivers;

    private Integer maxReceiverNum;

    private Long voteIdx;

    private Long voteOwnerIdx;

    private boolean fixVote;

    private String voteTitle;

    private List<ShownVoteRecord> records;

    private Integer maxNum;

    private boolean alreadyVoted;

    private Long chatRoomIdx;

}
