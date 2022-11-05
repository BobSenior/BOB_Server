package com.bob_senior.bob_server.configuration.controller;


import com.bob_senior.bob_server.domain.Chat.ChatDto;
import com.bob_senior.bob_server.domain.Chat.ChatPage;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.service.ChatService;
import com.bob_senior.bob_server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Controller
public class MessageController {

    private final ChatService chatService;
    private final UserService userService;

    @Autowired
    MessageController(ChatService chatService,UserService userService){
        this.userService = userService;
        this.chatService = chatService;
    }

    //1. 채팅을 페이지 단위로 받아오기
    @GetMapping("/chat/load/{roomId}")
    public BaseResponse<ChatPage> getChatRecordByPage(@PathVariable int roomId, final Pageable pageable){
        //pageable = requestParam으로 받음
        //format :
        try {
            chatService.loadChatPageData(pageable,roomId);
            //해당 room의 최근 x개의 채팅을 load
        } catch (Exception e) {
            e.printStackTrace();
            //TODO : 예외처리?
        }
        ChatPage chats = null;
        try {
            chats = chatService.loadChatPageData(pageable,roomId);
        } catch (Exception e) {
            //no page Exception..
        }
        return new BaseResponse<>(chats);
    }

    //2. 해당 채팅방에서 읽지 않은 채팅 개수 구하기
    @GetMapping("/chat/unread/{roomId}")
    public BaseResponse getUnreadChatNum(@PathVariable int roomId, int userIdx){
        //해당 유저가 valid한지 먼저 확인
        if(!userService.checkUserExist(userIdx)){
            //TODO : 유저 존재하지 않을 경우 handling - exception을 던져도 되고
            return new BaseResponse(BaseResponseStatus.INVALID_USER);

        }
        if(!chatService.checkUserParticipantChatting(roomId,userIdx)){
            //TODO : 유저가 채팅방에 존재하지 않을시 처리
            return new BaseResponse(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        return new BaseResponse<>(chatService.getNumberOfUnreadChatByUserIdx(userIdx,roomId));
    }

    //test method
    @GetMapping("test")
    public void addMsg(){
        ChatDto chat = ChatDto.builder()
                .senderIdx(1111)
                .type("MESSAGE")
                .channelId("2222")
                .data("epoch")
                .build();
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        chatService.storeNewMessage(chat,ts,1);
    }

}
