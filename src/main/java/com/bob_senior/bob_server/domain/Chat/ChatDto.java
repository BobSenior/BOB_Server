package com.bob_senior.bob_server.domain.Chat;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatDto {

    private String type;
    private Integer senderIdx;
    private String channelId;
    private String data;

    public ChatDto(){

    }

    public void newConnect(){
        this.type = "new";
    }

    public void closeConnect(){
        this.type = "close";
    }

}
