package com.bob_senior.bob_server.configuration.controller;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.vote.*;
import com.bob_senior.bob_server.service.ChatService;
import com.bob_senior.bob_server.service.UserService;
import com.bob_senior.bob_server.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
public class AppointmentController {

    private final ChatService chatService;
    private final VoteService voteService;
    private final UserService userService;

    @Autowired
    public AppointmentController(ChatService chatService, VoteService voteService, UserService userService) {
        this.chatService = chatService;
        this.voteService = voteService;
        this.userService = userService;
    }

    //투표의 생성은 websocket? 일반 url?
    @MessageMapping("/vote/{roomIdx}")
    @SendTo("/topic/room/{roomId}")
    public BaseResponse makeNewVoteToRoom(@DestinationVariable Integer roomIdx, MakeVoteDTO makeVoteDTO){
        if(!userService.checkUserExist(makeVoteDTO.getMakerIdx())){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
            //...boilerplate....
        }
        if(!chatService.checkUserParticipantChatting(roomIdx, makeVoteDTO.getMakerIdx())){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        //고민사항 1. 투표의 개시를 무조건 글쓴사람만 할 수 있게 할것인가?
        //일단 러프하게 그냥 생성하는 방식으로 구현.. 수정가능
        //TODO : 투표의 시작에 대한 필터링?
        try{
            LocalDateTime ldt = LocalDateTime.now();
            ShownVoteDTO vnr = voteService.makeNewVote(makeVoteDTO,ldt,roomIdx);
            return new BaseResponse(
                    VoteResult.builder()
                            .voteIdx(vnr.getVote().getVoteIdx())
                            .createdAt(vnr.getVote().getCreatedAt())
                            .title(vnr.getVote().getVoteName())
                            .tuples(vnr.getRecords())
                            .total_participated(0).build()
            );
        }
        catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
            //이게문제네.....
        }
    }

    @MessageMapping("/vote/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public BaseResponse makeVote(@DestinationVariable int roomId, UserVoteDTO userVoteDTO){
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
