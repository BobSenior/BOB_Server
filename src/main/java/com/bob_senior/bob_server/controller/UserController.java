package com.bob_senior.bob_server.controller;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.user.*;
import com.bob_senior.bob_server.repository.PostParticipantRepository;
import com.bob_senior.bob_server.repository.PostRepository;
import com.bob_senior.bob_server.service.AppointmentService;
import com.bob_senior.bob_server.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Log4j2
@RestController
public class UserController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final PostParticipantRepository postParticipantRepository;
    private final PostRepository postRepository;

    @Autowired
    public UserController(UserService userService, AppointmentService appointmentService, PostParticipantRepository postParticipantRepository, PostRepository postRepository) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.postParticipantRepository = postParticipantRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/test")
    public BaseResponse testString(){
        System.out.println("postRepository = " + postRepository.tete());
       return new BaseResponse(BaseResponseStatus.SUCCESS);
    }






    /**
     * FriendShip
     **/

    //해당 유저에게 친구추가 요청  **NOTICE make
    @PostMapping("/user/friendship/request")
    public BaseResponse makeFriendshipRequestByUserIdx(@RequestBody RequestFriendshipDTO requestFriendshipDTO){
        if(!userService.checkUserExist(requestFriendshipDTO.getRequesterIdx())){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            userService.makeNewFriendshipRequest(requestFriendshipDTO.getRequesterIdx(),requestFriendshipDTO.getTargetUUID());
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }




    //자신에게 온 친구추가목록 확인 **NOTICE 해제
    @GetMapping("/user/friendship/check")
    public BaseResponse getMyRequestedFriendShip(@RequestParam Long userIdx, Pageable pageable){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            List<SimplifiedUserProfileDTO> data = userService.getRequestedFriendShipWaiting(userIdx, pageable);
            return new BaseResponse(data);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }




    //친구 요청 처리 -> boolean으로 결정 ** NOTICE MAKE
    @PostMapping("/user/friendship/determine")
    public BaseResponse determineFriendshipRequest(@RequestBody DetermineFriendshipDTO determineFriendshipDTO){
        if(!userService.checkUserExist(determineFriendshipDTO.getUserIdx())){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            userService.determineFriendRequest(determineFriendshipDTO.getUserIdx(),determineFriendshipDTO.getTargetIdx(),determineFriendshipDTO.isAccept());
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    @GetMapping("/user/friendship/list")
    public BaseResponse getMyFriendshipList(@RequestParam Long userIdx,Pageable pageable){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            List<SimplifiedUserProfileDTO> list = userService.getFriendList(userIdx,pageable);
            return new BaseResponse(list);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    @PostMapping("/user/block")
    public BaseResponse makeBlockUser(@RequestBody RequireBlockDTO requireBlockDTO){
        if(!userService.checkUserExist(requireBlockDTO.getMyIdx())){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            userService.makeBlock(requireBlockDTO.getMyIdx(),requireBlockDTO.getBlockUserIdx());
            return new BaseResponse(BaseResponseStatus.SUCCESS);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }



}