package com.bob_senior.bob_server.controller;

import com.bob_senior.bob_server.domain.appointment.entity.TotalNotice;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.chat.ChatDto;
import com.bob_senior.bob_server.domain.chat.ChatPage;
import com.bob_senior.bob_server.domain.chat.SessionAndClientRecord;
import com.bob_senior.bob_server.domain.chat.ShownChat;
import com.bob_senior.bob_server.domain.chat.entity.SessionRecord;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.repository.PostRepository;
import com.bob_senior.bob_server.repository.SessionRecordRepository;
import com.bob_senior.bob_server.service.ChatService;
import com.bob_senior.bob_server.service.UserService;
import com.bob_senior.bob_server.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@RestController
public class ChatController {

    private final UserService userService;
    private final ChatService chatService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final VoteService voteService;
    private final SessionRecordRepository sessionRecordRepository;
    private final PostRepository postRepository;

    @Autowired
    public ChatController(SimpMessageSendingOperations simpMessageSendingOperations,
                          UserService userService, ChatService chatService, VoteService voteService, SessionRecordRepository sessionRecordRepository, PostRepository postRepository) {
        this.userService = userService;
        this.chatService = chatService;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
        this.voteService = voteService;
        this.sessionRecordRepository = sessionRecordRepository;
        this.postRepository = postRepository;
    }


    //??? ????????? ????????? api
    @PostMapping("/stomp/record/{roomIdx}")
    public BaseResponse recordUserSessionIdAndClientData(@PathVariable Long roomIdx, @RequestBody SessionAndClientRecord sessionAndClientRecord){
        //???????????? ????????? ?????? ??? api??? ?????? -> (sessionId, UserIdx, roomIdx)??? ??????
        System.out.println("sessionAndClientRecord = " + sessionAndClientRecord);
        System.out.println("roomIdx = " + roomIdx);
        chatService.activateChatParticipation(sessionAndClientRecord.getUserIdx(),roomIdx);
        long chatroom = postRepository.findPostByPostIdx(roomIdx).getChatRoomIdx();
        sessionRecordRepository.save(
                SessionRecord.builder()
                .sessionId(sessionAndClientRecord.getSessionId())
                .chatIdx(chatroom)
                .userIdx(sessionAndClientRecord.getUserIdx())
                .build());
        return new BaseResponse(BaseResponseStatus.SUCCESS);
    }




    //???????????? ??????
    @MessageMapping("/stomp/init/{roomIdx}")
    @SendTo("/topic/room/{roomIdx}")
    public BaseResponse enterChatRoom(ChatDto msg, @DestinationVariable Long roomIdx){
        //1. ?????? ????????? ????????? ???????????? ??????
        Long userIdx = msg.getSenderIdx();
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        if(chatService.checkUserParticipantChatting(userIdx,roomIdx)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        Timestamp ts = chatService.userParticipant(roomIdx,userIdx);
        String nickname = userService.getNickNameByIdx(msg.getSenderIdx());
        msg.setData(nickname + " ?????? ?????????????????????!");
        return new BaseResponse<ChatDto>(msg);
    }




    //???????????????
    @MessageMapping("/stomp/{roomIdx}")
    @SendTo("/topic/room/{roomIdx}")
    public BaseResponse sendChatToMembers(ChatDto msg, @DestinationVariable Long roomIdx){
        System.out.println("msg = " + msg);
        Long user = msg.getSenderIdx();
        if(!userService.checkUserExist(user)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        //?????? ????????? ????????? ???????????? ?????? -> room?????? user???????
        if(!chatService.checkUserParticipantChatting(roomIdx,user)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        Long nowDate = System.currentTimeMillis();
        Timestamp timeStamp = new Timestamp(nowDate);

        //?????? db??? ??????
        ShownChat shownChat = chatService.storeNewMessage(msg,timeStamp,roomIdx);

        return new BaseResponse<ShownChat>(shownChat);
    }




    //????????? ?????????
    @MessageMapping("/stomp/exit/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public BaseResponse exitChatRoom(@DestinationVariable Long roomId, Long sender){
        if(!userService.checkUserExist(sender)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        if(!chatService.checkUserParticipantChatting(roomId,sender)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        ChatDto msg = new ChatDto();
        msg.setSenderIdx(sender);
        //?????? sender?????? ????????? ??????????????? ??????\
        chatService.deleteUserFromRoom(roomId, sender);
        String senderNick = userService.getNickNameByIdx(sender);
        msg.setData(senderNick + " ?????? ?????????????????????");
        return new BaseResponse<ChatDto>(msg);
    }






    // ????????? ????????? ????????? ????????????
    @GetMapping("/chat/load/{roomId}")
    public BaseResponse getChatRecordByPage(@PathVariable Long roomId, Pageable pageable){
        //pageable = requestParam?????? ??????
        //format :
        try {
            chatService.loadChatPageData(pageable,roomId);
            //?????? room??? ?????? x?????? ????????? load
        } catch (Exception e) {
            e.printStackTrace();
            //TODO : ?????????????
        }
        ChatPage chats = null;
        try {
            List<ShownChat> data = chatService.loadChatPageData(pageable,roomId);
            return new BaseResponse(data);
        } catch (Exception e) {
            //no page Exception..
            return new BaseResponse(BaseResponseStatus.TAG_DOES_NOT_EXIST);
        }
    }




    //2. ?????? ??????????????? ?????? ?????? ?????? ?????? ?????????
    //????????? ?????? ????????? ?????? ?????? ????????? ?????? ?????????????????? ???????????? ???
    @GetMapping("/chat/unread/{roomId}")
    public BaseResponse getUnreadChatNum(@PathVariable Long roomId, @RequestParam Long userIdx){
        //?????? ????????? valid?????? ?????? ??????
        if(!userService.checkUserExist(userIdx)){
            //TODO : ?????? ???????????? ?????? ?????? handling - exception??? ????????? ??????
            return new BaseResponse(BaseResponseStatus.INVALID_USER);

        }
        if(!chatService.checkUserParticipantChatting(roomId,userIdx)){
            //TODO : ????????? ???????????? ???????????? ????????? ??????
            return new BaseResponse(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        return new BaseResponse<>(chatService.getNumberOfUnreadChatByUserIdx(userIdx,roomId,true));
        //return new BaseResponse(chatService.getTotalNumberOfUnreadChatByUserIdx(userIdx));
    }


    @GetMapping("/chat/unread/total")
    public BaseResponse getAllUnreadChatCount(@RequestParam Long userIdx){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            long count = chatService.getAllUnreadChatNum(userIdx);
            TotalNotice tn = TotalNotice.builder().totalCount(count).build();
            return new BaseResponse(tn);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }


}
