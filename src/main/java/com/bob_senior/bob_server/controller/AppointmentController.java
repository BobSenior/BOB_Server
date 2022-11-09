package com.bob_senior.bob_server.controller;

import com.bob_senior.bob_server.domain.appointment.AppointmentParticipantReqDTO;
import com.bob_senior.bob_server.domain.appointment.HandleRequestDTO;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.service.AppointmentService;
import com.bob_senior.bob_server.service.ChatService;
import com.bob_senior.bob_server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Controller
public class AppointmentController {

    private final UserService userService;
    private final ChatService chatService;
    private final AppointmentService appointmentService;


    @Autowired
    public AppointmentController(UserService userService, ChatService chatService, AppointmentService appointmentService) {
        this.userService = userService;
        this.chatService = chatService;
        this.appointmentService = appointmentService;
    }

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

    //현재 대기중인 head들 가져오기
    @GetMapping("/appointment/waiting")
    public BaseResponse getMyWaitingParticipantList(@RequestParam Integer userIdx,
                                                    Pageable pageable){

        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        //해당 유저 데이터의 wating상태의 head를 전부 가져오기
         try{
             return new BaseResponse(appointmentService.getUserWaitingAppointment(userIdx,pageable));
         }catch(BaseException e){
             return new BaseResponse(e.getStatus());
         }
    }

    //현재 참여중인 post의 head들을 가져오기
    @GetMapping("/appointment/ongoing")
    public BaseResponse getMyParticipatedAppointmentList(@RequestParam Integer userIdx,
                                                         Pageable pageable){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            return new BaseResponse(appointmentService.getUserParticipatedAppointment(userIdx,pageable));
            //pageable?
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    //참가요청하기
    @PostMapping("/appointment/request")
    public BaseResponse makeParticipantRequest(@RequestBody AppointmentParticipantReqDTO appointmentParticipantReqDTO){
        try{
            appointmentService.makeNewPostParticipation(appointmentParticipantReqDTO.getPostIdx(),appointmentParticipantReqDTO.getUserIdx());
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
        return new BaseResponse("passed");
    }

    //현 post에 걸린 참가요청 리스트 받아오기
    @GetMapping("/appointment/waiting/{postIdx}")
    public BaseResponse getRequestedParticipationHeadList(@PathVariable Integer postIdx, @RequestBody Integer userIdx,
                                                          Pageable pageable){
        //1. 유효한 user인지
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        //2. 유효한 postIdx인지
        if(!appointmentService.isPostExist(postIdx)){
            return new BaseResponse(BaseResponseStatus.NON_EXIST_POSTIDX);
        }
        //3. 해당 post의 owner인지
        if(!appointmentService.isOwnerOfPost(userIdx,postIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_ACCESS_TO_APPOINTMENT);
        }
        // 모든 검증 통과시 리스트 가져오기
        return new BaseResponse(appointmentService.getAllRequestInPost(postIdx,pageable));
    }

    //해당 참가 요청 거절 or 수락 -> 이건 그냥 boolean 값을 받으면 될듯
    @PostMapping("/appointment/determine/{postIdx}")
    public BaseResponse setUserRequestToAcceptOrReject(@PathVariable Integer postIdx, @RequestBody HandleRequestDTO handleRequestDTO){
        //1. 의사결정자의 userIdx가 올바른지 확인
        if(!(userService.checkUserExist(handleRequestDTO.getRequesterIdx()))){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        //2. 유효한 postIdx인지
        if(!appointmentService.isPostExist(postIdx)){
            return new BaseResponse(BaseResponseStatus.NON_EXIST_POSTIDX);
        }
        //3. 결정자가 해당 post의 owner가 아닐경우 reject
        if(!appointmentService.isOwnerOfPost(handleRequestDTO.getMakerIdx(), postIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_ACCESS_TO_APPOINTMENT);
        }
        //위의 모든 검증 통과시 해당 user의 postParticipant를 reject or accept - true일시 accept false일시 reject
        try {
            appointmentService.determineRequestStatus(postIdx, handleRequestDTO.getRequesterIdx(), handleRequestDTO.isAccept());
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch (BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }


}
