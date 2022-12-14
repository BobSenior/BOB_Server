package com.bob_senior.bob_server.controller;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.user.DetermineFriendshipDTO;
import com.bob_senior.bob_server.domain.user.RequestFriendshipDTO;
import com.bob_senior.bob_server.domain.user.RequireBlockDTO;
import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import com.bob_senior.bob_server.domain.email.EmailAuthRequestDTO;
import com.bob_senior.bob_server.domain.email.EmailAuthResDTO;
import com.bob_senior.bob_server.domain.user.*;
import com.bob_senior.bob_server.domain.user.entity.LoginResDTO;
import com.bob_senior.bob_server.service.MailService;
import com.bob_senior.bob_server.service.AppointmentService;
import com.bob_senior.bob_server.service.UserService;
import lombok.extern.log4j.Log4j2;
import com.bob_senior.bob_server.repository.PostParticipantRepository;
import com.bob_senior.bob_server.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

import static com.bob_senior.bob_server.domain.base.BaseResponseStatus.*;


@Log4j2
@RestController
public class UserController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final MailService mailService;
    private final PostParticipantRepository postParticipantRepository;
    private final PostRepository postRepository;

    @Autowired
    public UserController(UserService userService, AppointmentService appointmentService, MailService mailService, PostParticipantRepository postParticipantRepository, PostRepository postRepository) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.mailService = mailService;
        this.postParticipantRepository = postParticipantRepository;
        this.postRepository = postRepository;
    }

    @RequestMapping("/test")
    public String testString(){
        try{
            return "testpage";
        }catch (Exception e){
            log.error(e);
            return "error";
        }
    }

    @RequestMapping("/health")
    public String healthString(){
        try{
            return "?????? ????????? ?????? port 8081";
        }catch (Exception e){
            log.error(e);
            return "error";
        }
    }


    //????????????
    @PostMapping("/signUp")
    public BaseResponse<CreateUserResDTO> createUser (@RequestBody CreateUserReqDTO createUserReqDTO){
        //?????? ????????? ????????? ???????????? ?????? ???????????????
        try{
            return new BaseResponse<>( userService.registerUser(createUserReqDTO));
        }catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }

    }


    //????????? ?????? ??????
    @GetMapping("/confirm-mail")
    public BaseResponse<EmailAuthResDTO> confirmEmail(@ModelAttribute EmailAuthRequestDTO emailAuthRequestDTO){
        try{
            EmailAuthResDTO result = mailService.authMail(emailAuthRequestDTO);
            return new BaseResponse<>(result);
        }
        catch (BaseException e){
            log.error(" API : /confirm-mail" + "\n Message : " + e.getMessage() + "\n Cause : " + e.getCause());
            return new BaseResponse<>(e.getStatus());
        }

    }    //????????? ?????? ??????


    @GetMapping("/nickname/dupli")
    public BaseResponse<CheckNicknameResDTO> checkNickname(@RequestParam("nickname") String nickname) {
        // ????????? validation
        if (nickname == null) {
            return new BaseResponse<>(SIGNUP_EMPTY_USER_NICKNAME);
        }
        else{
            String pattern = "^([???-???a-zA-Z0-9]{2,10})$";

            if (!Pattern.matches(pattern, nickname)){
                return new BaseResponse<>(SIGNUP_INVALID_USER_NICKNAME);
            }
        }

        try {
            CheckNicknameResDTO result = userService.checkNickname(nickname);
            return new BaseResponse<>(result);
        } catch(BaseException e){
            log.error(" API : api/nickname/dupli" + "\n Message : " + e.getMessage() + "\n Cause : " + e.getCause());
            return new BaseResponse<>(e.getStatus());
        }

    }

    //id ?????? ??????
    @GetMapping("/id/dupli")
    public BaseResponse<CheckNicknameResDTO> checkId(@RequestParam("id") String id) {
        // ????????? validation
        if (id == null) {
            return new BaseResponse<>(SIGNUP_EMPTY_USER_ID);
        }
        else{
            String pattern = "^([a-zA-Z0-9]{2,10})$";

            if (!Pattern.matches(pattern, id)){
                return new BaseResponse<>(SIGNUP_INVALID_USER_ID);
            }
        }

        try {
            CheckNicknameResDTO result = userService.checkId(id);
            return new BaseResponse<>(result);
        } catch(BaseException e){
            log.error(" API : api/id/dupli" + "\n Message : " + e.getMessage() + "\n Cause : " + e.getCause());
            return new BaseResponse<>(e.getStatus());
        }

    }


    @PostMapping("/login")
    public BaseResponse<LoginResDTO> loginId (@RequestBody LoginReqDTO loginIdReqDTO){
        //????????? ?????? ?????? ????????????


        try{
            LoginResDTO result = userService.loginUser(loginIdReqDTO);
            return new BaseResponse<>(result);
        }catch (BaseException e){
            log.error(" API : api/login" + "\n Message : " + e.getMessage() + "\n Cause : " + e.getCause());
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * FriendShip
     **/

    //?????? ???????????? ???????????? ??????  **NOTICE make
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



    //???????????? ??? ?????????????????? ?????? **NOTICE ??????
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




    //?????? ?????? ?????? -> boolean?????? ??????  ** NOTICE MAKE
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