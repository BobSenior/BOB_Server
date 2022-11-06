package com.bob_senior.bob_server.domain.Post;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "Post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postIdx;

    @Column(name = "writerIdx")
    private Integer writerIdx;

    @Column(name = "title")
    private String title;

    @Column(name = "place")
    private String place;

    @Column(name = "content")
    private String content;

    @Column(name = "recruitmentStatus")
    private String recruitmentStatus;

    @Column(name = "registeredAt")
    private Timestamp registeredAt;

    @Column(name = "viewCount")
    private Integer viewCount;

    @Column(name="meetingDate")
    private LocalDateTime meetingDate;
}