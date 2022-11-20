package com.bob_senior.bob_server.domain.chat;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatPage {

private final Integer curPage;
private final Integer length;
private final List<ChatDto> chatList;

}
