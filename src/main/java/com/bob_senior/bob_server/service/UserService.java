package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.notice.entity.Notice;
import com.bob_senior.bob_server.domain.user.*;
import com.bob_senior.bob_server.domain.user.entity.*;
import com.bob_senior.bob_server.repository.BlockRepository;
import com.bob_senior.bob_server.repository.FriendshipRepository;
import com.bob_senior.bob_server.repository.NoticeRepository;
import com.bob_senior.bob_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final FriendshipRepository friendshipRepository;
    private final NoticeRepository noticeRepository;

    @Autowired
    UserService(UserRepository userRepository, BlockRepository blockRepository, FriendshipRepository friendshipRepository, NoticeRepository noticeRepository){
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;

        this.friendshipRepository = friendshipRepository;
        this.noticeRepository = noticeRepository;
    }

    public boolean checkUserExist(Long userIdx){
        return userRepository.existsById(userIdx);
    }

    public String getNickNameByIdx(Long userIdx) {
        User user = userRepository.findUserByUserIdx(userIdx);
        return user.getNickName();
    }

    //해당 유저에게 친구요청 해보기
    public void makeNewFriendshipRequest(Long requesterIdx, String targetUUID) throws BaseException {
        User user = userRepository.findByUuid(targetUUID);
        //1. 이미 친구 추가 된 경우 확인
        if(friendshipRepository.existsByFriendInfoAndAndStatus(new FriendId(requesterIdx,user.getUserIdx()),"ACTIVE")){
            throw new BaseException(BaseResponseStatus.ALREADY_HAS_FRIENDSHIP);
        }

        //2. 차단 여부 확인하기
        boolean hasBlocked = blockRepository.existsByBlockInfo(new BlockId(user.getUserIdx(),requesterIdx));
        if(hasBlocked){
            throw new BaseException(BaseResponseStatus.CAN_NOT_REQUEST_FRIENDSHIP);
        }

        //3. 이미 친구 요청이 보내졌는지 확인.. 할필요 없이 그냥 덮어버리기?
        friendshipRepository.save(
                Friendship.builder()
                        .friendInfo(new FriendId(requesterIdx,user.getUserIdx()))
                        .status("WAITING")
                        .build()
        );
        noticeRepository.save(
                Notice.builder()
                        .postIdx(0L)
                        .userIdx(user.getUserIdx())
                        .flag(0)
                        .type("friendRequest")
                        .content("친구 요청이 왔습니다")
                        .build()
        );
    }

    public List<SimplifiedUserProfileDTO> getRequestedFriendShipWaiting(Long userIdx, Pageable pageable) throws BaseException{
        //해당 유저에게 들어온 친구추가 요청을 확인하기..어찌넘길까
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
        //자신에게 온 친구요청의 notice를 해제
        noticeRepository.disableFriendRequestNotice("friendRequest",userIdx);

        return data;
    }

    public void determineFriendRequest(Long userIdx, Long targetIdx, boolean accept) throws BaseException{
        //해당 유저의 request를 처리하기
        FriendId friendId = new FriendId(userIdx,targetIdx);
        boolean already = friendshipRepository.existsByFriendInfo_MaxUserIdxAndFriendInfo_MinUserIdxAndStatus(friendId.getMinUserIdx(),friendId.getMinUserIdx(),"ACTIVE");
        if(already){
            throw new BaseException(BaseResponseStatus.ALREADY_HAS_FRIENDSHIP);
        }
        if(!friendshipRepository.existsByFriendInfo(friendId)){
            //애초에 요청이 없던 케이스
            throw new BaseException(BaseResponseStatus.INVALID_USER_TO_ACCEPT);
        }
        //아닐시 friendship을 active로 업데이트
        String content = "";
        String type = "";
        if(accept) {
            //승락시 active로 업데이트
            friendshipRepository.updateFriendShipACTIVE(friendId);
            content = "친구요청이 승락되었습니다";
            type = "friendRequest_accept";
        }
        else{
            Friendship friendship = friendshipRepository.getTopByFriendInfoAndStatus(friendId,"WAITING");
            friendshipRepository.delete(friendship);
            content = "친구요청이 거절되었습니다";
            type = "friendRequest_reject";
        }
        noticeRepository.save(
                Notice.builder()
                        .userIdx(targetIdx)
                        .postIdx(0L)
                        .type(type)
                        .content(content)
                        .flag(0)
                        .build()
        );
    }

    public void makeBlock(Long myIdx, Long blockUserIdx) throws BaseException{
        //block tuple을 생성
        BlockId info = new BlockId(myIdx,blockUserIdx);
        if(blockRepository.existsByBlockInfo(info)){
            //이미 block에 존재
            throw new BaseException(BaseResponseStatus.ALREADY_BLOCKED_USER);
        }
        //존재하지 않을경우 block을 저장
        Block block = Block.builder().blockInfo(info).build();
        blockRepository.save(block);

        //이후 friendship에 해당 user존재시 tuple삭제
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
}
