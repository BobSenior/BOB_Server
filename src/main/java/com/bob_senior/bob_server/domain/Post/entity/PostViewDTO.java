package com.bob_senior.bob_server.domain.Post.entity;

import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostViewDTO {

    private String title;

    private String groupConstraint;

    private String location;

    private Timestamp meetingAt;

    private List<SimplifiedUserProfileDTO> buyer;

    private List<SimplifiedUserProfileDTO> receiver;

    private String contents;

    private List<String> tagHead;

    private boolean isRequested;

}