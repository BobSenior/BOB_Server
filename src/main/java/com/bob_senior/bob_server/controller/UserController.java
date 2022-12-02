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


    //회원가입
    @PostMapping("/signUp")
    public BaseResponse<CreateUserResDTO> createUser (@RequestBody CreateUserReqDTO createUserReqDTO){
        //학교 이메일 맞는지 확인하는 로직 추가해야함
        return new BaseResponse<>( userService.registerUser(createUserReqDTO));
    }


    //이메일 인증 완료
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

    }    //이메일 인증 완료


    @GetMapping("/nickname/dupli")
    public BaseResponse<CheckNicknameResDTO> checkNickname(@RequestParam("nickname") String nickname) {
        // 형식적 validation
        if (nickname == null) {
            return new BaseResponse<>(SIGNUP_EMPTY_USER_NICKNAME);
        }
        else{
            String pattern = "^([가-힣a-zA-Z0-9]{2,10})$";

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

    //id 중복 체크
    @GetMapping("/id/dupli")
    public BaseResponse<CheckNicknameResDTO> checkId(@RequestParam("id") String id) {
        // 형식적 validation
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
        //이메일 인증 상태 확인하기


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




    //친구 요청 처리 -> boolean으로 결정  ** NOTICE MAKE
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