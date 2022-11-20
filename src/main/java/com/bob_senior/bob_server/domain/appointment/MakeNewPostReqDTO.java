package com.bob_senior.bob_server.domain.appointment;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MakeNewPostReqDTO {

    private Long writerIdx;

    private String title;

    private String location;

    private LocalDateTime meetingAt;

    private String type;

    private Integer receiverNum;

    private Integer buyerNum;

    //초대된 buyer의 목록
    private List<Long> invitedIdx;

    private String constraint;

    private String content;

    private List<String> tags;


}
