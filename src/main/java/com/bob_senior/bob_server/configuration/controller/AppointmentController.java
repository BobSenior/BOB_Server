package com.bob_senior.bob_server.configuration.controller;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.user.User;
import com.bob_senior.bob_server.service.AppointmentService;
import com.bob_senior.bob_server.service.ChatService;
import com.bob_senior.bob_server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
