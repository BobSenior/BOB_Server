package com.bob_senior.bob_server.domain.chat;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatDto {

    /**
     * nickname, data, Timestamp(적힌시간)
     */

    private Long senderIdx;
    private String data;

    public ChatDto(){

    }


}
