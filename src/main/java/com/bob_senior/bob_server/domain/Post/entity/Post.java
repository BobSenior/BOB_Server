package com.bob_senior.bob_server.domain.Post.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "Post")
@DynamicInsert
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postIdx;

    @Column(name = "writerIdx")
    private Long writerIdx;

    @Column(name = "title")
    private String title;

    @Column(name = "place")
    private String place;

    @Column(name = "content")
    private String content;

    @Column(name = "recruitmentStatus")
    private String recruitmentStatus;

    @Column(name = "registerdAt")
    private Timestamp registeredAt;

    @Column(name = "viewCount")
    private Integer viewCount;

    @Column(name="meetingDate")
    private LocalDateTime meetingDate;

    @Column
    private String meetingType;

    @Column
    private Integer participantLimit;

    @Column
    private String participantConstraint;

    @Column
    private Long chatRoomIdx;

    @OneToMany(mappedBy = "post")
    private List<PostParticipant> participantList = new ArrayList<>();

    @Column
    private Integer maxBuyerNum;

    @Column
    private Integer maxReceiverNum;

    public boolean contains(){
        return false;
    }

}
