package com.bob_senior.bob_server.domain.chat;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatPage {
//이새낀가 그냥 list로 처리하셧죠 이놈이네
    //page하고 length필요없으면
    //걍 baseResponse안에 바로 list박는게 더 낫죠 ㅇㅇ 그래서 page하고 length빼면
    //바로 될거같긴한데 ㅇㅋㅇㅋ
private final Integer curPage;
private final Integer length;
private final List<ShownChat> chatList;

}
