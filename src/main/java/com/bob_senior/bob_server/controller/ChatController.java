package com.bob_senior.bob_server.controller;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.chat.ChatDto;
import com.bob_senior.bob_server.domain.chat.ChatPage;
import com.bob_senior.bob_server.domain.chat.SessionAndClientRecord;
import com.bob_senior.bob_server.domain.chat.ShownChat;
import com.bob_senior.bob_server.domain.chat.entity.SessionRecord;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.user.UserIdxDTO;
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

@Slf4j
@RestController
public class ChatController {

    private final UserService userService;
    private final ChatService chatService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final VoteService voteService;
    private final SessionRecordRepository sessionRecordRepository;

    @Autowired
    public ChatController(SimpMessageSendingOperations simpMessageSendingOperations,
                          UserService userService, ChatService chatService, VoteService voteService, SessionRecordRepository sessionRecordRepository) {
        this.userService = userService;
        this.chatService = chatService;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
        this.voteService = voteService;
        this.sessionRecordRepository = sessionRecordRepository;
    }


    //첫 연결시 거치는 api
    @MessageMapping("/stomp/record/{roomIdx}")
    public BaseResponse recordUserSessionIdAndClientData(@DestinationVariable Long roomIdx, SessionAndClientRecord sessionAndClientRecord){
        //웹소켓이 연결된 직후 이 api로 전송 -> (sessionId, UserIdx, roomIdx)를 저장
        chatService.activateChatParticipation(sessionAndClientRecord.getUserIdx(),roomIdx);
        sessionRecordRepository.save(new SessionRecord(sessionAndClientRecord.getSessionId(),sessionAndClientRecord.getUserIdx(),roomIdx));
        return new BaseResponse(BaseResponseStatus.SUCCESS);
    }




    //채팅방에 참여
    @MessageMapping("/stomp/init/{roomIdx}")
    @SendTo("/topic/room/{roomIdx}")
    public BaseResponse enterChatRoom(ChatDto msg, @DestinationVariable Long roomIdx){
        //1. 새로 들어온 유저를 채팅방에 등록
        Long userIdx = msg.getSenderIdx();
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        if(chatService.checkUserParticipantChatting(userIdx,roomIdx)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        Timestamp ts = chatService.userParticipant(roomIdx,userIdx);
        String nickname = userService.getNickNameByIdx(msg.getSenderIdx());
        msg.setData(nickname + " 님이 입장하셨습니다!");
        return new BaseResponse<ChatDto>(msg);
    }




    //채팅보내기
    @MessageMapping("/stomp/{roomIdx}")
    @SendTo("/topic/room/{roomIdx}")
    public BaseResponse sendChatToMembers(ChatDto msg, @DestinationVariable Long roomIdx){
        Long user = msg.getSenderIdx();
        if(!userService.checkUserExist(user)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        //해당 유저가 유효한 유저인지 검사 -> room내의 user인지?
        if(!chatService.checkUserParticipantChatting(roomIdx,user)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        Long nowDate = System.currentTimeMillis();
        Timestamp timeStamp = new Timestamp(nowDate);

        //채팅 db에 저장
        ShownChat shownChat = chatService.storeNewMessage(msg,timeStamp,roomIdx);

        return new BaseResponse<ShownChat>(shownChat);
    }




    //채팅방 나가기
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
        //해당 sender유저 채팅방 데이터에서 제거\
        chatService.deleteUserFromRoom(roomId, sender);
        String senderNick = userService.getNickNameByIdx(sender);
        msg.setData(senderNick + " 님이 퇴장하셨습니다");
        return new BaseResponse<ChatDto>(msg);
    }






    // 채팅을 페이지 단위로 받아오기
    @GetMapping("/chat/load/{roomId}")
    public BaseResponse<ChatPage> getChatRecordByPage(@PathVariable Long roomId, Pageable pageable){
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
    //아니면 해당 유저가 읽지 않은 개수를 모두 구해오는것도 가능하긴 함
    @GetMapping("/chat/unread/{roomId}")
    public BaseResponse getUnreadChatNum(@PathVariable Long roomId, @RequestParam Long userIdx){
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
        //return new BaseResponse(chatService.getTotalNumberOfUnreadChatByUserIdx(userIdx));
    }


    @GetMapping("/chat/unread/total")
    public BaseResponse getAllUnreadChatCount(@RequestParam Long userIdx){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            return new BaseResponse(chatService.getAllUnreadChatNum(userIdx));
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }


}
