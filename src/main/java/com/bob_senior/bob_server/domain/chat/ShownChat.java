package com.bob_senior.bob_server.domain.chat;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShownChat {
    private String nickname;

    private long senderIdx;

    private String writtenAt;

    private String content;

}
