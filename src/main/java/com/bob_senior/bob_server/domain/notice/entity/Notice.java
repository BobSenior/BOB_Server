package com.bob_senior.bob_server.domain.notice.entity;

import lombok.*;

import javax.persistence.*;

@Entity(name = "Notice")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeIdx;

    @Column
    private Long userIdx;

    @Column
    private Long postIdx;

    @Column
    private Integer flag;

    @Column
    private String content;

    @Column
    private String type;

}
