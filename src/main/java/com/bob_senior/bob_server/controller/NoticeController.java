package com.bob_senior.bob_server.controller;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponse;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.notice.ShownNotice;
import com.bob_senior.bob_server.domain.user.UserIdxDTO;
import com.bob_senior.bob_server.service.NoticeService;
import com.bob_senior.bob_server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NoticeController {

    private final UserService userService;
    private final NoticeService noticeService;

    @Autowired
    public NoticeController(UserService userService, NoticeService noticeService) {
        this.userService = userService;
        this.noticeService = noticeService;
    }

    @GetMapping("/notice/total")
    public BaseResponse getTotalActivatingNoticeNum(@RequestParam Long userIdx){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            long totalCount = noticeService.getTotalActivatingNoticeForUser(userIdx);
            return new BaseResponse(totalCount);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }

    @GetMapping("/notice/list")
    public BaseResponse getMyNoticeList(@RequestParam Long userIdx){
        if(!userService.checkUserExist(userIdx)){
            return new BaseResponse(BaseResponseStatus.INVALID_USER);
        }
        try{
            List<ShownNotice> list = noticeService.getMyNoticeList(userIdx);
            return new BaseResponse(list);
        }catch(BaseException e){
            return new BaseResponse(e.getStatus());
        }
    }




}
