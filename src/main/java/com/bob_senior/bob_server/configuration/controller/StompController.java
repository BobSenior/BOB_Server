package com.bob_senior.bob_server.configuration.controller;

import com.bob_senior.bob_server.domain.Chat.ChatDto;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.vote.UserVoteDTO;
import com.bob_senior.bob_server.domain.vote.VoteResult;
import com.bob_senior.bob_server.service.ChatService;
import com.bob_senior.bob_server.service.UserService;
import com.bob_senior.bob_server.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.sql.Timestamp;

@Slf4j
@Controller
public class StompController {

    private final UserService userService;
    private final ChatService chatService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final VoteService voteService;

    @Autowired
    public StompController(SimpMessageSendingOperations simpMessageSendingOperations,
    UserService userService, ChatService chatService,VoteService voteService) {
        this.userService = userService;
        this.chatService = chatService;
        this.simpMessageSendingOperations = simpMessageSendingOperations;
        this.voteService = voteService;
    }

    @MessageMapping("/stomp/init/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public BaseResponse enterChatRoom(ChatDto msg, @DestinationVariable int roomId){
        //1. 새로 들어온 유저를 채팅방에 등록
        Integer userIdx = msg.getSenderIdx();
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        if(chatService.checkUserParticipantChatting(userIdx,roomId)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        Timestamp ts = chatService.userParticipant(roomId,userIdx);
        msg.setType("INIT");
        String nickname = userService.getNickNameByIdx(msg.getSenderIdx());
        msg.setData(nickname + " 님이 입장하셨습니다!");
        return new BaseResponse<ChatDto>(msg);
    }

    @MessageMapping("/stomp/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public BaseResponse sendChatToMembers(ChatDto msg, @DestinationVariable int roomId){
        //verify_if_chatroom_exist(roomId);
        Integer user = msg.getSenderIdx();
        if(!userService.checkUserExist(user)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        //해당 유저가 유효한 유저인지 검사 -> room내의 user인지?
        if(!chatService.checkUserParticipantChatting(roomId,user)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        Long nowDate = System.currentTimeMillis();
        Timestamp timeStamp = new Timestamp(nowDate);

        //채팅 db에 저장
        chatService.storeNewMessage(msg,timeStamp,roomId);

        return new BaseResponse<ChatDto>(msg);
    }

    @MessageMapping("/stomp/exit/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public BaseResponse exitChatRoom(@DestinationVariable int roomId, Integer sender){
        if(!userService.checkUserExist(sender)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        if(!chatService.checkUserParticipantChatting(roomId,sender)){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        ChatDto msg = new ChatDto();
        msg.setSenderIdx(sender);
        msg.setType("EXIT");
        //해당 sender유저 채팅방 데이터에서 제거\
        chatService.deleteUserFromRoom(roomId, sender);
        String senderNick = userService.getNickNameByIdx(sender);
        msg.setData(senderNick + " 님이 퇴장하셨습니다");
        return new BaseResponse<ChatDto>(msg);
    }

    @MessageMapping("/stomp/vote/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public BaseResponse voteFromUser(@DestinationVariable int roomId, UserVoteDTO userVoteDTO){
        //1. 해당 유저가 적절한지 먼저 검사
        if(!userService.checkUserExist(userVoteDTO.getUserIdx())){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        //2. 해당 유저가 해당 방에 참가중인지 검사
        if(!chatService.checkUserParticipantChatting(roomId,userVoteDTO.getUserIdx())){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        //3. 해당 vote가 valid한지 검사
        if(!voteService.checkIfVoteIsValid(roomId,userVoteDTO.getVoteIdx())){
            //invalid vote Exception
            return new BaseResponse<>(BaseResponseStatus.ALREADY_VOTED);
        }
        try{
            VoteResult vr = voteService.applyUserSelectionToVote(userVoteDTO);
            return new BaseResponse<VoteResult>(vr);
        }
        catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

}
