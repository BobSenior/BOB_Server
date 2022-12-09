package com.bob_senior.bob_server.controller;

import com.bob_senior.bob_server.domain.Post.entity.PostViewDTO;
import com.bob_senior.bob_server.domain.appointment.*;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.user.KickUserDTO;
import com.bob_senior.bob_server.domain.user.UserIdxDTO;
import com.bob_senior.bob_server.service.AppointmentService;
import com.bob_senior.bob_server.service.ChatService;
import com.bob_senior.bob_server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
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


    /**
     *
     * POST
     *
     */

    @PostMapping("/post/write")
    public BaseResponse makeNewPostB(@RequestBody MakeNewPostReqDTO makeNewPostReqDTO){
        if(!userService.checkUserExist(makeNewPostReqDTO.getWriterIdx())){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            long count = appointmentService.makeNewPost(makeNewPostReqDTO);
            return new BaseResponse(count);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }

    }


    //내가 참여할 수 있는 appointment들을 가져오기
    @GetMapping("/post/list")
    public BaseResponse getReachableAppointmentPage(Pageable pageable,@RequestParam Long userIdx){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        List<AppointmentHeadDTO> getter = appointmentService.getAvailableAppointmentList(userIdx,pageable);
        return new BaseResponse(getter);
    }


    //post의 홈화면 가져오기
    @GetMapping("/post/{postIdx}")
    public BaseResponse getPostHomeView(@PathVariable Long postIdx,@RequestParam Long userIdx ){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        if(!appointmentService.isPostExist(postIdx)){
            return new BaseResponse(BaseResponseStatus.NON_EXIST_POSTIDX);
        }
        try{
            PostViewDTO data = appointmentService.getPostData(postIdx,userIdx);
            return new BaseResponse(data);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    //현재 신청 대기중인 게시글 head(page)
    @GetMapping("/post/waiting")
    public BaseResponse getMyWaitingParticipantList(@RequestParam Long userIdx,
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

    //참가요청하기 ** make NOTICE
    @PostMapping("/post/request")
    public BaseResponse makeParticipantRequest(@RequestBody AppointmentParticipantReqDTO appointmentParticipantReqDTO){
        try{
            appointmentService.makeNewPostParticipation(appointmentParticipantReqDTO.getPostIdx(),appointmentParticipantReqDTO.getUserIdx(),appointmentParticipantReqDTO.getPosition());
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
        return new BaseResponse(BaseResponseStatus.SUCCESS);
    }

    @PostMapping("/post/request/reverse")
    public BaseResponse makeRequestReverse(@RequestBody RequestDrawbackDTO requestDrawbackDTO){
        if(!userService.checkUserExist(requestDrawbackDTO.getUserIdx())){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        //2. 유효한 postIdx인지
        if(!appointmentService.isPostExist(requestDrawbackDTO.getPostIdx())){
            return new BaseResponse(BaseResponseStatus.NON_EXIST_POSTIDX);
        }

        try{
            appointmentService.drawbackRequest(requestDrawbackDTO.getUserIdx(),requestDrawbackDTO.getPostIdx());
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }


    //현 post에 걸린 참가요청 리스트 받아오기
    @GetMapping("/post/request/waiting/{postIdx}")
    public BaseResponse getRequestedParticipationHeadList(@PathVariable Long postIdx, @RequestParam Long userIdx,
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
        return new BaseResponse(appointmentService.getAllRequestInPost(userIdx,postIdx,pageable));
    }



    //주어진 검색어로 title기반 search
    @GetMapping("/post/search")
    public BaseResponse getPostSearchResult(@RequestParam Long userIdx, @RequestParam String searchString,Pageable pageable){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            List<AppointmentHeadDTO> heads = appointmentService.searchByStringInTitle(userIdx,searchString,pageable);
            return new BaseResponse(heads);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    //tag를 통한 search -> multi-tag?
    @GetMapping("/post/search/tags")
    public BaseResponse getPostSearchResultByTags(@RequestParam Long userIdx,@RequestParam String tag,Pageable pageable ){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            List<AppointmentHeadDTO> heads = appointmentService.searchByTag(userIdx,tag,pageable);
            return new BaseResponse(heads);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }


    /**
     *
     * APPOINTMENT
     *
     */



    //해당 약속 홈화면 정보 가져오기 **Notice disable!!
    @GetMapping("/appointment/{postIdx}")
    public BaseResponse getAppointmentHomeView(@PathVariable Long postIdx,@RequestParam Long userIdx){
        System.out.println("hihihi");
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        if(!chatService.checkUserParticipantChatting(postIdx, userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_CHATROOM_ACCESS);
        }
        try{
            return new BaseResponse(appointmentService.getAppointmentData(postIdx,userIdx));
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }





    //현재 참여중인 약속의 head들을 가져오기
    @GetMapping("/appointment/ongoing")
    public BaseResponse getMyParticipatedAppointmentList(@RequestParam Long userIdx,
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


    //해당 참가 요청 거절 or 수락 -> 이건 그냥 boolean 값을 받으면 될듯 ** NOTICE
    @PostMapping("/appointment/determine/{postIdx}")
    public BaseResponse setUserRequestToAcceptOrReject(@PathVariable Long postIdx, @RequestBody HandleRequestDTO handleRequestDTO){
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



    //초대기능 -> 무조건 방장만 할 수 있게 ** NOTICE MAKE
    @PostMapping("/appointment/invite/{postIdx}")
    public BaseResponse inviteUserIntoPostByUUID(@PathVariable Long postIdx, @RequestBody UserInviteDTO inviteDTO){
        if(!(userService.checkUserExist(inviteDTO.getInviterIdx()))){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        //2. 유효한 postIdx인지
        if(!appointmentService.isPostExist(postIdx)){
            return new BaseResponse(BaseResponseStatus.NON_EXIST_POSTIDX);
        }
        //3. 일단은 방의 주인만 초대할 수 있도록 설정 -> 채팅으로 uuid넘겨주게 해야될듯
        if(!appointmentService.isOwnerOfPost(inviteDTO.getInviterIdx(), postIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_ACCESS_TO_APPOINTMENT);
        }
        try{
            appointmentService.inviteUserByUUID(inviteDTO.getInvitedUUID(),postIdx,inviteDTO.getPosition());
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }


    @PostMapping("/appointment/leave/{postIdx}") //** NOTICE?
    public BaseResponse leaveAppointmentParticipant(@PathVariable long postIdx, @RequestBody UserIdxDTO userIdxDTO){
        long userIdx = userIdxDTO.getUserIdx();
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        if(!appointmentService.checkIfUserParticipating(postIdx, userIdx)){
            return new BaseResponse(BaseResponseStatus.IS_NOT_PARTICIPANT_OF_APPOINTMENT);
        }
        try{
            appointmentService.exitAppointment(postIdx,userIdx);
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }


    //강퇴기능
    @PostMapping("/appointment/kick/{postIdx}")
    public BaseResponse kickUserInAppointment(@PathVariable long postIdx, @RequestBody KickUserDTO kickUserDTo){
        if((!userService.checkUserExist(kickUserDTo.getKickedIdx()))||(!userService.checkUserExist(kickUserDTo.getKickerIdx()))){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        if(!appointmentService.isPostExist(postIdx)){
            return new BaseResponse(BaseResponseStatus.NON_EXIST_POSTIDX);
        }
        if(!appointmentService.isOwnerOfPost(kickUserDTo.getKickerIdx(), postIdx)){
            return new BaseResponse(BaseResponseStatus.IS_NOT_OWNER_OF_APPOINTMENT);
        }
        if(kickUserDTo.getKickedIdx() == kickUserDTo.getKickerIdx()){
            return new BaseResponse(BaseResponseStatus.INVALID_KICK_USER_SELF);
        }
        try{
            appointmentService.kickUser(postIdx, kickUserDTo.getKickerIdx(),kickUserDTo.getKickedIdx());
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }



}
