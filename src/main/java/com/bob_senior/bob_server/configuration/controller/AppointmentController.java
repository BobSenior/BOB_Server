package com.bob_senior.bob_server.configuration.controller;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.vote.*;
import com.bob_senior.bob_server.service.AppointmentService;
import com.bob_senior.bob_server.service.ChatService;
import com.bob_senior.bob_server.service.UserService;
import com.bob_senior.bob_server.service.VoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

@Slf4j
@Controller
public class AppointmentController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final VoteService voteService;
    private final UserService userService;
    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(SimpMessagingTemplate messagingTemplate, ChatService chatService, VoteService voteService, UserService userService, AppointmentService appointmentService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.voteService = voteService;
        this.userService = userService;
        this.appointmentService = appointmentService;
    }

    /**
     *
     * VOTE
     *
     */


    @GetMapping("/appointment/vote/{roomIdx}")
    public BaseResponse getCurrentActivatingVote(@PathVariable Integer roomIdx){
        if(!voteService.hasActivatedVoteInRoom(roomIdx)){
            return new BaseResponse(BaseResponseStatus.NO_VOTE_IN_CHATROOM);
        }
        //투표가 존재시 가장 최근의 투표 1개만 가져온다.
        try{
            return new BaseResponse(voteService.getMostRecentVoteInChatroom(roomIdx));
        }
        catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }




    //투표의 생성은 websocket? 일반 url?
    @MessageMapping("/vote/init/{roomIdx}")
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
                    ShownVote.builder()
                            .voteIdx(vnr.getVote().getVoteIdx())
                            .createdAt(vnr.getVote().getCreatedAt())
                            .title(vnr.getVote().getTitle())
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
            ShownVote vr = voteService.applyUserSelectionToVote(userVoteDTO);
            return new BaseResponse<ShownVote>(vr);
        }
        catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    @MessageMapping("/vote/terminate/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public BaseResponse terminateVote(@DestinationVariable int roomId,TerminateVoteDTO terminateVoteDTO){
        //Q)terminate이후 바로 투표내용을 적용할것인가?
        //TODO : problem1 - 투표 결과 동률이 나올경우 어찌 처리할것인가? 2. 바로 반영 or 발주자가 알아서?
        if(!userService.checkUserExist(terminateVoteDTO.getTerminatorIdx())){
            return new BaseResponse<>(BaseResponseStatus.INVALID_USER);
        }
        if(!chatService.checkUserParticipantChatting(roomId,terminateVoteDTO.getTerminatorIdx())){
            return new BaseResponse<>(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        try{

            voteService.makeTerminateVote(roomId,terminateVoteDTO);
            return new BaseResponse("clear");

        } catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
        //vote의 종료를 어떻게 알릴것인가?
    }

    /**
     * appointment
     */

    @GetMapping("/appointment/{roomIdx}")
    public BaseResponse getAppointmentHomeView(@PathVariable Integer roomIdx, Integer userIdx){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        if(!chatService.checkUserParticipantChatting(roomIdx, userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        try{
            return new BaseResponse(appointmentService.getAppointmentData(roomIdx));
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }


    }

}
