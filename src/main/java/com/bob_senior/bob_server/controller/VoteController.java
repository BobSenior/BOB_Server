package com.bob_senior.bob_server.controller;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

@Slf4j
@Controller
public class VoteController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final VoteService voteService;
    private final UserService userService;
    private final AppointmentService appointmentService;

    @Autowired
    public VoteController(SimpMessagingTemplate messagingTemplate, ChatService chatService, VoteService voteService, UserService userService, AppointmentService appointmentService) {
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


    //현재 채팅방에 activated의 vote리스트 가져오기
    @GetMapping("/appointment/vote/list/{roomIdx}")
    public BaseResponse getCurrentActivatingVoteList(@PathVariable Integer roomIdx,Integer userIdx){
        if(!voteService.hasActivatedVoteInRoom(roomIdx)){
            return new BaseResponse(BaseResponseStatus.NO_VOTE_IN_CHATROOM);
        }
        //투표가 존재시 가장 최근의 투표 1개만 가져온다.
        //option 2 : 현재 activated인 리스트를 전부 가져와서 head만 보여주고, 각 head를 선택하면 투표화면으로 넘어가게?
        try{
            return new BaseResponse(voteService.getMostRecentVoteInChatroom(roomIdx,userIdx));
        }
        catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }




    //특정vote데이터 가져오기
    @GetMapping("/appointment/vote/{roomIdx}/{voteIdx}")
    public BaseResponse getSpecificVoteData(@PathVariable Integer roomIdx,@PathVariable Integer voteIdx){
        //1. 해당 vote가 유효한지 검사
        if(!voteService.checkIfVoteIsValid(roomIdx, voteIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_VOTE_ACCESS);
        }
        //2. 그냥 투표정보 가져오면 되나..
        //투표정보(카톡 레퍼런스) : 게시자 정보, title, records, total participated
        try {
            return new BaseResponse(voteService.getVoteByVoteIdx(voteIdx));
        }catch (BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }






    //투표의 생성은 websocket? 일반 url?
    /*@MessageMapping("/vote/init/{roomIdx}")
    @SendTo("/topic/room/{roomId}")*/
    @PostMapping("/vote/init/{roomIdx}")
    public BaseResponse makeNewVoteToRoom(@PathVariable Integer roomIdx, MakeVoteDTO makeVoteDTO){
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
            /*return new BaseResponse(
                    vnr
                    //만약 stomp로 한다면 이거 추가 필요..
            );*/
            return new BaseResponse<>(BaseResponseStatus.SUCCESS);
            //일반 postMappint으로 할 경우.. 그냥 성공화면만 띄워주고 직접 투표리스트에 가서 선택하도록
        }
        catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
            //이게문제네.....
        }
    }

    /*@MessageMapping("/vote/{roomId}")
    @SendTo("/topic/room/{roomId}")*/
    @PostMapping("/vote/{roomId}")
    public BaseResponse makeVote(@PathVariable int roomId, UserVoteDTO userVoteDTO){
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
            ShownVoteDTO vr = voteService.applyUserSelectionToVote(userVoteDTO);
            return new BaseResponse<ShownVoteDTO>(vr);
            //이건 투표결과가 바로 반영되도록 데이터를 return해준다
            //다른방법 : front에서 optiministic 사용하면 되긴함..
        }
        catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    /*@MessageMapping("/vote/terminate/{roomId}")
    @SendTo("/topic/room/{roomId}")*/
    @PostMapping("/vote/terminate/{roomId}")
    public BaseResponse terminateVote(@PathVariable int roomId,TerminateVoteDTO terminateVoteDTO){
        //Q)terminate이후 바로 투표내용을 적용할것인가?
        //TODO : problem1 - 투표 결과 동률이 나올경우 어찌 처리할것인가?   2. 바로 반영 or owner가 알아서?
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
}
