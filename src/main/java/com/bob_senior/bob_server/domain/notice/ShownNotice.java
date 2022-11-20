package com.bob_senior.bob_server.domain.notice;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShownNotice {

    private Long postIdx;

    private String type;

    private String text;

}
