package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.email.entity.EmailAuth;
import com.bob_senior.bob_server.domain.email.entity.SchoolEmail;
import com.bob_senior.bob_server.domain.notice.entity.Notice;
import com.bob_senior.bob_server.domain.user.*;
import com.bob_senior.bob_server.domain.user.entity.*;
import com.bob_senior.bob_server.repository.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.bob_senior.bob_server.domain.base.BaseResponseStatus.*;

@Service
@Log4j2
public class UserService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final FriendshipRepository friendshipRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final SchoolEmailRepository schoolEmailRepository;
    private final MailService mailService;
    private final NoticeRepository noticeRepository;
    private final JwtService jwtService;

    @Autowired
    UserService(UserRepository userRepository, BlockRepository blockRepository, FriendshipRepository friendshipRepository, EmailAuthRepository emailAuthRepository, SchoolEmailRepository schoolEmailRepository, MailService mailService,
                NoticeRepository noticeRepository, JwtService jwtService){
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
        this.emailAuthRepository = emailAuthRepository;
        this.schoolEmailRepository = schoolEmailRepository;
        this.mailService = mailService;
        this.noticeRepository = noticeRepository;
        this.friendshipRepository = friendshipRepository;
        this.jwtService = jwtService;
    }

    public boolean checkUserExist(Long userIdx){
        return userRepository.existsById(userIdx);
    }

    public String getNickNameByIdx(Long userIdx) {
        User user = userRepository.findUserByUserIdx(userIdx);
        return user.getNickName();
    }

    //?????? ???????????? ???????????? ?????????
    public void makeNewFriendshipRequest(Long requesterIdx, String targetUUID) throws BaseException {
        User user = userRepository.findByUuid(targetUUID);
        //1. ?????? ?????? ?????? ??? ?????? ??????
        if(friendshipRepository.existsByFriendInfoAndAndStatus(new FriendId(requesterIdx,user.getUserIdx()),"ACTIVE")){
            throw new BaseException(BaseResponseStatus.ALREADY_HAS_FRIENDSHIP);
        }

        //2. ?????? ?????? ????????????
        boolean hasBlocked = blockRepository.existsByBlockInfo(new BlockId(user.getUserIdx(),requesterIdx));
        if(hasBlocked){
            throw new BaseException(BaseResponseStatus.CAN_NOT_REQUEST_FRIENDSHIP);
        }

        //3. ?????? ?????? ????????? ??????????????? ??????.. ????????? ?????? ?????? ????????????????
        friendshipRepository.save(
                Friendship.builder()
                        .friendInfo(new FriendId(requesterIdx,user.getUserIdx()))
                        .status("WAITING")
                        .build()
        );
    }

    public List<SimplifiedUserProfileDTO> getRequestedFriendShipWaiting(Long userIdx, Pageable pageable) throws BaseException{
        //?????? ???????????? ????????? ???????????? ????????? ????????????..???????????????
        List<Friendship> list = friendshipRepository.findAllByUserIdxInWaiting(userIdx,pageable).getContent();
        List<SimplifiedUserProfileDTO> data = new ArrayList<>();
        for (Friendship friendship : list) {
            Long other = friendship.getFriendInfo().getMinUserIdx() == userIdx?friendship.getFriendInfo().getMaxUserIdx() : friendship.getFriendInfo().getMinUserIdx();
            User user = userRepository.findUserByUserIdx(other);
            data.add(
                    SimplifiedUserProfileDTO.builder()
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .department(user.getDepartment())
                            .schoolId(user.getSchoolId())
                            .school(user.getSchool())
                            .build()
            );
        }
        //???????????? ??? ??????????????? notice??? ??????
        noticeRepository.disableFriendRequestNotice("friendRequest",userIdx);

        return data;
    }

    public void determineFriendRequest(Long userIdx, Long targetIdx, boolean accept) throws BaseException{
        //?????? ????????? request??? ????????????
        FriendId friendId = new FriendId(userIdx,targetIdx);
        boolean already = friendshipRepository.existsByFriendInfo_MaxUserIdxAndFriendInfo_MinUserIdxAndStatus(friendId.getMinUserIdx(),friendId.getMinUserIdx(),"ACTIVE");
        if(already){
            throw new BaseException(BaseResponseStatus.ALREADY_HAS_FRIENDSHIP);
        }
        if(!friendshipRepository.existsByFriendInfo(friendId)){
            //????????? ????????? ?????? ?????????
            throw new BaseException(BaseResponseStatus.INVALID_USER_TO_ACCEPT);
        }
        //????????? friendship??? active??? ????????????
        String content = "";
        String type = "";
        if(accept) {
            //????????? active??? ????????????
            friendshipRepository.updateFriendShipACTIVE(friendId);
            content = "??????????????? ?????????????????????";
            type = "friendRequest_accept";
        }
        else{
            Friendship friendship = friendshipRepository.getTopByFriendInfoAndStatus(friendId,"WAITING");
            friendshipRepository.delete(friendship);
            content = "??????????????? ?????????????????????";
            type = "friendRequest_reject";
        }
    }

    public void makeBlock(Long myIdx, Long blockUserIdx) throws BaseException{
        //block tuple??? ??????
        BlockId info = new BlockId(myIdx,blockUserIdx);
        if(blockRepository.existsByBlockInfo(info)){
            //?????? block??? ??????
            throw new BaseException(BaseResponseStatus.ALREADY_BLOCKED_USER);
        }
        //???????????? ???????????? block??? ??????
        Block block = Block.builder().blockInfo(info).build();
        blockRepository.save(block);

        //?????? friendship??? ?????? user????????? tuple??????
        friendshipRepository.delete(
                Friendship.builder()
                        .friendInfo(
                                new FriendId(myIdx,blockUserIdx)
                        )
                        .status("ACTIVATE")
                        .build()
        );
    }
    public List<SimplifiedUserProfileDTO> getFriendList(long userIdx, Pageable pageable)  throws BaseException {

        List<Friendship> list = friendshipRepository.getFriendList(userIdx,pageable).getContent();
        List<SimplifiedUserProfileDTO> userProfileDTOList = new ArrayList<>();
        for (Friendship friendship : list) {
            long oppositeIdx = friendship.getFriendInfo().getMaxUserIdx()==userIdx ? friendship.getFriendInfo().getMinUserIdx() : friendship.getFriendInfo().getMaxUserIdx();
            User user = userRepository.findUserByUserIdx(oppositeIdx);
            userProfileDTOList.add(
                    SimplifiedUserProfileDTO.builder()
                            .userIdx(user.getUserIdx())
                            .department(user.getDepartment())
                            .school(user.getSchool())
                            .schoolId(user.getSchoolId()).build()
            );
        }
        return userProfileDTOList;
    }

    public CreateUserResDTO registerUser(CreateUserReqDTO createUserReqDTO) throws BaseException{
        //????????? ???????????? ????????? ??????
        String userEmail = createUserReqDTO.getEmail();
        String userEmailBack = userEmail.substring(userEmail.indexOf("@")+1,userEmail.length());

        SchoolEmail schoolEmailEntity = schoolEmailRepository.findBySchoolName(createUserReqDTO.getSchool());

        log.info(userEmail.length());
        log.info(userEmailBack);

        if(schoolEmailEntity == null){
            throw new BaseException(SCHOOL_NAME_NOT_REGISTERED);
        }

        if(!schoolEmailEntity.getSchoolEmail().equals(userEmailBack)){
            throw new BaseException(EMAIL_NOT_MATCHED);
        }



        User user = User.builder().department(createUserReqDTO.getDepartment())
                .email(createUserReqDTO.getEmail())
                .password(createUserReqDTO.getPassword())
                .nickName(createUserReqDTO.getNickName())
                .school(createUserReqDTO.getSchool())
                .schoolId(createUserReqDTO.getSchoolId())
                .imageURL("BobSenior")
                .userId(createUserReqDTO.getUserId())
                .uuid(UUID.randomUUID().toString())
                .department(createUserReqDTO.getDepartment())
                .build();

        userRepository.save(user);

        EmailAuth emailAuth = new EmailAuth(createUserReqDTO.getEmail(),UUID.randomUUID().toString(),"false");

        emailAuthRepository.save(emailAuth);
        // ????????? ??????
        String addr = "bobseniorcs@gmail.com";


        String subject = "????????? ????????????";

        String body = "????????? ????????? ??????????????? ????????? ???????????????!\r\n" + "https://bobsenior.co.kr/confirm-mail/?email="+emailAuth.getEmail() + "&authToken="+emailAuth.getAuthToken();

        mailService.sendEmail(createUserReqDTO.getEmail(), addr, subject, body);
        //????????? ?????????

        return new CreateUserResDTO("??????????????? ??????????????? ????????? ????????????.");
    }

    public CheckNicknameResDTO checkNickname(String nickname) throws BaseException{

        boolean isNicknameExisted = false;
        try {
            isNicknameExisted = userRepository.existsByNickName(nickname);
        } catch (Exception e) {
            log.error("DATABASE_ERROR when call UserRepository.checkNickname()");
        }

        if (isNicknameExisted == false) {
            return new CheckNicknameResDTO(false, "?????? ????????? ??????????????????.");
        } else {
            log.error("ILLEGAL_ARG_ERROR when call UserRepository.checkNickname() because nickname is already used");
            throw new BaseException(SIGNUP_ALREADY_EXIST_NICKNAME);

        }
    }

    public CheckNicknameResDTO checkId(String id) throws BaseException{
        boolean isIdExisted = false;
        isIdExisted = userRepository.existsByUserId(id);

        if (isIdExisted == false) {
            return new CheckNicknameResDTO(false, "?????? ????????? ??????????????????.");
        } else {
            log.error("ILLEGAL_ARG_ERROR when call UserRepository.checkNickname() because nickname is already used");
            throw new BaseException(SIGNUP_ALREADY_EXIST_ID);
        }
    }

    public LoginResDTO loginUser(LoginReqDTO loginReqDTO) throws BaseException{
        boolean idAndPwExist = userRepository.existsByUserIdAndPassword(loginReqDTO.getUserId(), loginReqDTO.getPassword());
        if(idAndPwExist == false) {
            throw new BaseException(LOGIN_INFO_NOT_MATCH);
        }

        User user = userRepository.findByUserIdAndPassword(loginReqDTO.getUserId(), loginReqDTO.getPassword());

        if(user.getAuthorizedStatus().equals("I")){
            throw new BaseException(EMAIL_NOT_AUTHORIZED);
        }

        // JWT !!!!!
        String jwtAccessToken = jwtService.createAccessToken(Math.toIntExact(user.getUserIdx()));
        String jwtRefreshToken = jwtService.createRefreshToken(Math.toIntExact(user.getUserIdx()));


        return new LoginResDTO(
                "???????????? ?????????????????????.",
                Math.toIntExact(user.getUserIdx()),
                user.getNickName(),
                user.getUuid(),
                user.getSchoolId(),
                user.getDepartment(),
                user.getImageURL(),
                jwtAccessToken,
                jwtRefreshToken
        );
    }
}
