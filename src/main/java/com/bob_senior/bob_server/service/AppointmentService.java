package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.appointment.MakeNewPostReqDTO;
import com.bob_senior.bob_server.domain.chat.entity.ChatNUser;
import com.bob_senior.bob_server.domain.chat.entity.ChatParticipant;
import com.bob_senior.bob_server.domain.Post.entity.*;
import com.bob_senior.bob_server.domain.appointment.AppointmentHeadDTO;
import com.bob_senior.bob_server.domain.appointment.AppointmentViewDTO;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.chat.entity.ChatRoom;
import com.bob_senior.bob_server.domain.notice.entity.Notice;
import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import com.bob_senior.bob_server.domain.user.entity.User;
import com.bob_senior.bob_server.domain.vote.ShownVoteRecord;
import com.bob_senior.bob_server.domain.vote.entity.Vote;
import com.bob_senior.bob_server.domain.vote.entity.VoteRecord;
import com.bob_senior.bob_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final PostRepository postRepository;
    private final PostParticipantRepository postParticipantRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final PostTagRepository postTagRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final VoteRepository voteRepository;
    private final VoteParticipatedRepository voteParticipatedRepository;
    private final NoticeRepository noticeRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRepository chatRepository;


    @Autowired
    public AppointmentService(PostRepository postRepository, PostParticipantRepository postParticipantRepository, UserRepository userRepository, ChatParticipantRepository chatParticipantRepository, PostPhotoRepository postPhotoRepository, PostTagRepository postTagRepository, ChatRoomRepository chatRoomRepository, VoteRecordRepository voteRecordRepository, VoteRepository voteRepository, VoteParticipatedRepository voteParticipatedRepository, NoticeRepository noticeRepository, ChatMessageRepository chatMessageRepository, ChatRepository chatRepository) {
        this.postRepository = postRepository;
        this.postParticipantRepository = postParticipantRepository;
        this.userRepository = userRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.postPhotoRepository = postPhotoRepository;
        this.postTagRepository = postTagRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.voteRecordRepository = voteRecordRepository;
        this.voteRepository = voteRepository;
        this.voteParticipatedRepository = voteParticipatedRepository;
        this.noticeRepository = noticeRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRepository = chatRepository;
    }




    public AppointmentViewDTO getAppointmentData(Long postIdx,long userIdx) throws BaseException {
        Post post = postRepository.findPostByPostIdx(postIdx);

        List<SimplifiedUserProfileDTO> buyer = new ArrayList<>();
        List<SimplifiedUserProfileDTO> receiver = new ArrayList<>();

        List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(postIdx,"active","buyer");
        List<PostParticipant> receiver_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(postIdx,"active","receiver");
        for (PostParticipant participant : buyer_prev) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            buyer.add(
                    SimplifiedUserProfileDTO.builder()
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .schoolId(user.getSchoolId())
                            .isOnline(false)
                            .school(user.getSchool())
                            .department(user.getDepartment())
                            .build()
            );
        }
        for (PostParticipant participant : receiver_prev) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            receiver.add(
                    SimplifiedUserProfileDTO.builder()
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .schoolId(user.getSchoolId())
                            .isOnline(false)
                            .school(user.getSchool())
                            .department(user.getDepartment())
                            .build()
            );
        }


        Vote vote = voteRepository.getVoteByPostIdx(postIdx);

        List<ShownVoteRecord> records = new ArrayList<>();
        if(vote!=null) {

            List<VoteRecord> vr = voteRecordRepository.findAllByVoteId_VoteIdx(vote.getVoteIdx());

            for (VoteRecord voteRecord : vr) {
                records.add(
                        ShownVoteRecord.builder().id(voteRecord.getVoteId().getChoiceIdx()).content(voteRecord.getVoteContent()).count(voteRecord.getCount()).build()
                );
            }
        }

        //해당 post에 달린 모든 notice를 해제시킨다
        noticeRepository.disablePostRelatedNotice(postIdx,userIdx);

        if(voteRepository.findVoteByPostIdxAndIsActivated(postIdx,1) == null){
            return AppointmentViewDTO.builder()
                    .writerIdx(post.getWriterIdx())
                    .constraint(post.getParticipantConstraint())
                    .title(post.getTitle())
                    .postIdx(post.getPostIdx())
                    .location(post.getPlace())
                    .maxBuyerNum(post.getMaxBuyerNum())
                    .maxReceiverNum(post.getMaxReceiverNum())
                    .meetingAt(post.getMeetingDate())
                    .buyers(buyer)
                    .receivers(receiver)
                    .chatRoomIdx(post.getChatRoomIdx())
                    .build();
        }

        return AppointmentViewDTO.builder()
                .writerIdx(post.getWriterIdx())
                .constraint(post.getParticipantConstraint())
                .voteIdx(voteRepository.findVoteByPostIdxAndIsActivated(postIdx,1).getVoteIdx())
                .title(post.getTitle())
                .postIdx(post.getPostIdx())
                .location(post.getPlace())
                .maxBuyerNum(post.getMaxBuyerNum())
                .maxReceiverNum(post.getMaxReceiverNum())
                .meetingAt(post.getMeetingDate())
                .buyers(buyer)
                .receivers(receiver)
                .voteTitle(voteRepository.findVoteByPostIdxAndIsActivated(postIdx,1).getTitle())
                .records(records)
                .maxNum(buyer.size() + receiver.size())
                .alreadyVoted(voteParticipatedRepository.existsVoteParticipatedByUserIdxAndVote_VoteIdx(userIdx,vote.getVoteIdx()))
                .chatRoomIdx(post.getChatRoomIdx())
                .build();
    }




    public List<AppointmentHeadDTO> getUserWaitingAppointment(Long userIdx, Pageable pageable) throws BaseException{
        //해당 유저의 waiting상태의 request을 전부 가져오기
        //List<AppointmentRequest> appointmentList= appointmentRequestRepository.findAllByPostUser_UserIdxAndStatus(userIdx,"WAITING",pageable).getContent();
        List<PostParticipant> participantList = postParticipantRepository.findAllByUserIdxAndStatus(userIdx,"WAITING",pageable).getContent();

        List<AppointmentHeadDTO> data = new ArrayList<>();

        for (PostParticipant waiting : participantList) {
            Post post = postRepository.findPostByPostIdx(waiting.getPost().getPostIdx());

            long currNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"active");

            long waitingNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"waiting");

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());

            List<PostTag> tags = postTagRepository.findAllByPost_PostIdx(post.getPostIdx());
            List<String> heads = new ArrayList<>();
            for (PostTag tag : tags) {
                heads.add(tag.getTagContent());
            }



            SimplifiedUserProfileDTO writer_simp = SimplifiedUserProfileDTO.builder()
                    .userIdx(writer.getUserIdx())
                    .nickname(writer.getNickName())
                    .department(writer.getDepartment())
                            .schoolId(writer.getSchoolId())
                                    .school(writer.getSchool())
                                    .school(writer.getSchool())
                                            .build();

            data.add(
                    AppointmentHeadDTO.builder()
                            .postIdx(post.getPostIdx())
                            .title(post.getTitle())
                            .writtenAt(post.getRegisteredAt())
                            .imageURL(
                                    //postPhotoRepository.findPostPhotoByPost_PostIdx(post.getPostIdx()).getPostPhotoUrl()
                                    "test"
                            )
                            .writer(writer_simp)
                            .location(post.getPlace())
                            .meetingAt(post.getMeetingDate())
                            .type(post.getMeetingType())
                            .status(post.getRecruitmentStatus())
                            .totalNum(post.getParticipantLimit())
                            .currNum(currNum)
                            .waitingNum(waitingNum)
                            .tagHeads(heads)
                            .build()
            );
        }
        return data;
    }




    public List<AppointmentHeadDTO> getUserParticipatedAppointment(Long userIdx, Pageable pageable) throws BaseException{

        List<PostParticipant> list_participating = postParticipantRepository.findAllByUserIdxAndStatus(userIdx,"active",pageable).getContent();

        List<AppointmentHeadDTO> data = new ArrayList<>();

        for (PostParticipant participant : list_participating) {
            Post post = postRepository.findPostByPostIdx(participant.getPost().getPostIdx());

            long currNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"active");

            long waitingNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"waiting");

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());

            SimplifiedUserProfileDTO writer_simp = SimplifiedUserProfileDTO.builder()
                    .userIdx(writer.getUserIdx())
                    .nickname(writer.getNickName())
                    .department(writer.getDepartment())
                    .schoolId(writer.getSchoolId())
                    .school(writer.getSchool())
                    .build();

            data.add(
                    AppointmentHeadDTO.builder()
                            .postIdx(post.getPostIdx())
                            .title(post.getTitle())
                            .writtenAt(post.getRegisteredAt())
                            .imageURL(
                                    //postPhotoRepository.findPostPhotoByPost_PostIdx(post.getPostIdx()).getPostPhotoUrl()
                                "test"
                            )
                            .writer(writer_simp)
                            .location(post.getPlace())
                            .meetingAt(post.getMeetingDate())
                            .type(post.getMeetingType())
                            .status(post.getRecruitmentStatus())
                            .totalNum(post.getParticipantLimit())
                            .currNum(currNum)
                            .waitingNum(waitingNum)
                            .build()
            );
        }

        //마지막으로 해당 post의 주인에게 notice를 보낸다.
        return data;
    }




    @Transactional
    public void makeNewPostParticipation(Long postIdx, Long userIdx,String position) throws BaseException{
        //1. 일단 방이 존재하는지 확인
        boolean exist = postRepository.existsByPostIdxAndRecruitmentStatus(postIdx,"active");
        if(!exist) throw new BaseException(BaseResponseStatus.NON_EXIST_POSTIDX);

         //2. 해당 방에 이미 참여중인지 확인 or 이미 waiting인 상태가 존재하는지 or 이미 reject된게 존재하는지 -> 그냥 participant를 보면 되네
        boolean already_participated = postParticipantRepository.existsByPost_PostIdxAndUserIdx(postIdx,userIdx);
        if(already_participated) throw new BaseException(BaseResponseStatus.ALREADY_PARTICIPATED_IN_ROOM);

        //3. 이미 만료된(풀방, finished)인지 확인 -> 포지션별로 해야될듯..
        Long cur_participation_count = postParticipantRepository.countByPost_PostIdxAndPositionAndStatus(postIdx,position,"active");
        Post p = postRepository.findPostByPostIdx(postIdx);
        if(position.equals("buyer")){
            if(cur_participation_count>=p.getMaxBuyerNum()
                    || p.getRecruitmentStatus() == "FINISHED"){
                //이미 풀방 || recruitmentStatus가 이미 완료시 더이상 참여 불가능
                throw new BaseException(BaseResponseStatus.UNABLE_TO_MAKE_REQUEST_IN_POST);
            }
        }
        else{
            if(cur_participation_count>=p.getMaxReceiverNum()
                    || p.getRecruitmentStatus() == "FINISHED"){
                //이미 풀방 || recruitmentStatus가 이미 완료시 더이상 참여 불가능
                throw new BaseException(BaseResponseStatus.UNABLE_TO_MAKE_REQUEST_IN_POST);
            }
        }
        //4. 위의 verification 을 모두 통과시 participation 을 추가하기
        postParticipantRepository.save(PostParticipant.builder()
                .userIdx(userIdx)
                .post(postRepository.findPostByPostIdx(postIdx))
                .status("waiting")
                .position(position)
                .build());

        //5. 채널 주인에게 notice날리게 데이터 저장
        noticeRepository.save(
                Notice.builder()
                        .userIdx(postRepository.findPostByPostIdx(postIdx).getWriterIdx())
                        .postIdx(postIdx)
                        .flag(0)
                        .content(postIdx+"에 참가요청이 있습니다")
                        .type("request")
                       .build()
        );
    }




    public boolean isOwnerOfPost(Long userIdx, Long postIdx) {
        //내가 해당 게시글의 owner인지 확인
        Post post = postRepository.findPostByPostIdx(postIdx);
        return post.getWriterIdx() == userIdx;
    }




    public boolean isPostExist(Long postIdx) {
        return postRepository.existsByPostIdxAndRecruitmentStatus(postIdx,"active");
    }




    public List<SimplifiedUserProfileDTO> getAllRequestInPost(Long userIdx,Long postIdx, Pageable pageable) {
        //모든 request의 head를 가져온다.
        List<PostParticipant> list = postParticipantRepository.findAllByPost_PostIdxAndStatus(postIdx,"waiting",pageable).getContent();
        List<SimplifiedUserProfileDTO> data = new ArrayList<>();
        for (PostParticipant participant : list) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            data.add(SimplifiedUserProfileDTO.builder()
                    .userIdx(user.getUserIdx())
                    .nickname(user.getNickName())
                    .department(user.getDepartment())
                    .school(user.getSchool())
                    .schoolId(user.getSchoolId())
                    .build());
        }
        // request관련 알람 flag해제하기
        noticeRepository.disableFriendRequestNotice("request",userIdx);
        return data;
    }




    @Transactional
    public void determineRequestStatus(Long postIdx, Long requesterIdx, boolean accept) throws BaseException{
        //1. 해당 request가 존재했는지 확인하는게 우선
        boolean isExist = postParticipantRepository.existsByPost_PostIdxAndUserIdx(postIdx,requesterIdx);
        if(!isExist) throw new BaseException(BaseResponseStatus.NON_EXIST_POST_PARTICIPATION);
        Post post = postRepository.findPostByPostIdx(postIdx);
        int total = post.getParticipantLimit();
        long curr = postParticipantRepository.countByPost_PostIdxAndStatus(postIdx,"active");
        if(total - curr <=0){
            //더이상 참여 불가능 ->
            throw new BaseException(BaseResponseStatus.UNABLE_TO_PARTICIPATE_IN_POST);
        }
        //2. 존재할 경우 -> accept에 따라 다르게 처리
        String result = "";
        String type="";
        if(accept){
            //true -> 해당 request를 받아들일 경우
            //1. 일단 postParticipation을 participate로 변경
            postParticipantRepository.changePostParticipationStatus("active",postIdx,requesterIdx);
            //2. 그 후 해당 post의 chatroom에 추가 ->
            chatParticipantRepository.save(
                    ChatParticipant.builder()
                            .chatNUser(new ChatNUser(post.getChatRoomIdx(),requesterIdx))
                            .status("active")
                            .lastRead(null)
                            .build()
            );
            changeRecruitmentStatusIfFull(post, total, curr);
            type="accept";
            result = "참가요청이 수락되었습니다";
        }
        else{
            //false일 경우 -> requst를 거절
            postParticipantRepository.changePostParticipationStatus("reject",postIdx,requesterIdx);
            result = "참가요청이 거절되었습니다";
            type="reject";
        }
        noticeRepository.save(
                Notice.builder()
                        .postIdx(postIdx)
                        .userIdx(requesterIdx)
                        .flag(0)
                        .type(type)
                        .content(result)
                        .build()
        );
    }





    //해당 유저가 접근가능한 약속 리스트 전부 가져오기
    public List<AppointmentHeadDTO> getAvailableAppointmentList(Long userIdx, Pageable pageable) {
        //1. 유저의 소속정보 가져오기(school, dep, year)
        User user = userRepository.findUserByUserIdx(userIdx);
        LocalDateTime now = LocalDateTime.now();
        List<Post> posts = postRepository.getAllThatCanParticipant("active",user.getDepartment(),now,userIdx,pageable).getContent();
        System.out.println("posts = " + posts + pageable);
        List<AppointmentHeadDTO> data = new ArrayList<>();
        for (Post post : posts) {

            long currNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"active");

            long waitingNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"waiting");

            List<PostTag> tags = postTagRepository.findAllByPost_PostIdx(post.getPostIdx());
            List<String> heads = new ArrayList<>();
            for (PostTag tag : tags) {
                heads.add(tag.getTagContent());
            }

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());
            data.add(
                    AppointmentHeadDTO.builder()
                            .postIdx(post.getPostIdx())
                            .title(post.getTitle())
                            .imageURL(
                                    //postPhotoRepository.findPostPhotoByPost_PostIdx(post.getPostIdx()).getPostPhotoUrl()
                                    "test"
                            )
                            .writer(
                                    SimplifiedUserProfileDTO.builder()
                                            .userIdx(writer.getUserIdx())
                                            .nickname(writer.getNickName())
                                            .department(writer.getDepartment())
                                            .schoolId(writer.getSchoolId())
                                            .school(writer.getSchool())
                                            .build()
                            )
                            .location(post.getPlace())
                            .writtenAt(post.getRegisteredAt())
                            .meetingAt(post.getMeetingDate())
                            .type(post.getMeetingType())
                            .status(post.getRecruitmentStatus())
                            .totalNum(post.getParticipantLimit())
                            .currNum(currNum)
                            .waitingNum(waitingNum)
                            .tagHeads(heads)
                            .build()
            );
        }
    return data;
    }




    @Transactional
    //해당 postIdx로 uuid의 유저를 초대
    public void inviteUserByUUID(String invitedUUID, Long postIdx,String position) throws BaseException{
        //일단 postIdx에 참여 가능여부를 확인
        Post post = postRepository.findPostByPostIdx(postIdx);
        int total = 0;
        if(position.equals("buyer")){
            total = post.getMaxBuyerNum();
        }
        else total = post.getMaxReceiverNum();

        //현재 참여중인 user의 수
        long curr = postParticipantRepository.countByPost_PostIdxAndPositionAndStatus(postIdx,position,"active").intValue();

        //더이상 참여가 불가능한 경우
        if(total - curr <=0){
            //더이상 참여가 불가능
            throw new BaseException(BaseResponseStatus.UNABLE_TO_PARTICIPATE_IN_POST);
        }
        //uuid로 일단 유저 가져오기
        if(!userRepository.existsByUuid(invitedUUID)){
            throw new BaseException(BaseResponseStatus.INVALID_UUID_FOR_USER);
        }

        User user = userRepository.findByUuid(invitedUUID);

        //이미 참여중인 경우
        if(postParticipantRepository.existsByPost_PostIdxAndUserIdxAndStatus(postIdx,user.getUserIdx(),"active")){
            throw new BaseException(BaseResponseStatus.ALREADY_PARTICIPATED_IN_ROOM);
        }

        //참여시키기 1. postParticipant에 추가 + chatParticipant에 추가
        postParticipantRepository.save(PostParticipant.builder()
                .userIdx(user.getUserIdx())
                .status("active")
                .post(post)
                .position(position)
                .build()
        );


       chatParticipantRepository.save(
                ChatParticipant.builder()
                        .chatNUser(new ChatNUser(post.getChatRoomIdx(), user.getUserIdx()))
                        .status("active")
                        .build()
       );

        //참여 이후 만약 total과 같아질시 recruitment status를 "finish"로 바꾸기
        changeRecruitmentStatusIfFull(post, total, curr);
        noticeRepository.save(
                Notice.builder()
                        .postIdx(postIdx)
                        .userIdx(user.getUserIdx())
                        .flag(0)
                        .type("invite")
                        .content("게시글에 초대되었습니다")
                        .build()
        );
    }

    private void changeRecruitmentStatusIfFull(Post post, int total, long curr) {
        if(curr +1 == total){
            post.setRecruitmentStatus("finish");
            postRepository.save(post);
        }
    }



    //해당 string으로 검색하기 -> 타이틀 검색?
    public List<AppointmentHeadDTO> searchByStringInTitle(Long userIdx,String searchString, Pageable pageable) throws BaseException {
        List<AppointmentHeadDTO> heads = new ArrayList<>();
        User user = userRepository.findUserByUserIdx(userIdx);
        LocalDateTime now = LocalDateTime.now();
        String dep = userRepository.findUserByUserIdx(userIdx).getDepartment();
        //List<Post> list = postRepository.searchAllParticipantThatCanParticipant("active",dep,searchString,pageable).getContent();
        List<Post> list = postRepository.getAllParticipantThatCanParticipant(dep,searchString,now,userIdx,pageable).getContent();
        System.out.println("list = " + list);
        for (Post post : list) {
            long currNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"active");

            long waitingNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"waiting");

            List<PostTag> tags = postTagRepository.findAllByPost_PostIdx(post.getPostIdx());
            List<String> tghead = new ArrayList<>();
            for (PostTag tag : tags) {
                tghead.add(tag.getTagContent());
            }

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());
            heads.add(
                    AppointmentHeadDTO.builder()
                            .postIdx(post.getPostIdx())
                            .title(post.getTitle())
                            .writtenAt(post.getRegisteredAt())
                            .imageURL(
                                    //postPhotoRepository.findPostPhotoByPost_PostIdx(post.getPostIdx()).getPostPhotoUrl()
                                    "test"
                            )
                            .writer(
                                    SimplifiedUserProfileDTO.builder()
                                            .userIdx(writer.getUserIdx())
                                            .nickname(writer.getNickName())
                                            .department(writer.getDepartment())
                                            .schoolId(writer.getSchoolId())
                                            .school(writer.getSchool())
                                            .build()
                            )
                            .location(post.getPlace())
                            .meetingAt(post.getMeetingDate())
                            .type(post.getMeetingType())
                            .status(post.getRecruitmentStatus())
                            .totalNum(post.getParticipantLimit())
                            .currNum(currNum)
                            .waitingNum(waitingNum)
                            .tagHeads(tghead)
                            .build()
            );
        }
        return heads;
    }

    @Transactional
    public List<AppointmentHeadDTO> searchByTag(Long userIdx, String tag, Pageable pageable) throws BaseException{
        //1. tag의 존재 여부
        boolean isExist = postTagRepository.existsByTagContent(tag);
        if(!isExist) throw new BaseException(BaseResponseStatus.TAG_DOES_NOT_EXIST);

        //2. 해당 태그를 가지는 모든 post를 가져와서 가공
        List<AppointmentHeadDTO> heads = new ArrayList<>();
        String dep = userRepository.findUserByUserIdx(userIdx).getDepartment();
        List<PostTag> list = postTagRepository.searchTagThatCanParticipate(tag,dep,pageable).getContent();
        for (PostTag pt : list) {
            Post post = pt.getPost();
            long currNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"active");

            long waitingNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"waiting");

            List<PostTag> tags = postTagRepository.findAllByPost_PostIdx(post.getPostIdx());
            List<String> tghead = new ArrayList<>();
            for (PostTag tagg : tags) {
                tghead.add(tagg.getTagContent());
            }


            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());
            heads.add(
                    AppointmentHeadDTO.builder()
                            .postIdx(post.getPostIdx())
                            .title(post.getTitle())
                            .writtenAt(post.getRegisteredAt())
                            .imageURL(
                                    postPhotoRepository.findPostPhotoByPost_PostIdx(post.getPostIdx()).getPostPhotoUrl()
                            )
                            .writer(
                                    SimplifiedUserProfileDTO.builder()
                                            .userIdx(writer.getUserIdx())
                                            .nickname(writer.getNickName())
                                            .department(writer.getDepartment())
                                            .schoolId(writer.getSchoolId())
                                            .school(writer.getSchool())
                                            .build()
                            )
                            .location(post.getPlace())
                            .meetingAt(post.getMeetingDate())
                            .type(post.getMeetingType())
                            .status(post.getRecruitmentStatus())
                            .totalNum(post.getParticipantLimit())
                            .currNum(currNum)
                            .waitingNum(waitingNum)
                            .tagHeads(tghead)
                            .build()
            );
        }
        return heads;
    }

    @Transactional
    public PostViewDTO getPostData(Long roomIdx,Long userIdx) throws BaseException{
        //해당 post의 데이터 싸그리 가져오기
        Post post = postRepository.findPostByPostIdx(roomIdx);

        List<SimplifiedUserProfileDTO> buyer = new ArrayList<>();
        List<SimplifiedUserProfileDTO> receiver = new ArrayList<>();

        List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(roomIdx,"active","buyer");
        List<PostParticipant> receiver_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(roomIdx,"active","receiver");
        for (PostParticipant participant : buyer_prev) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            buyer.add(
                    SimplifiedUserProfileDTO.builder()
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .department(user.getDepartment())
                            .school(user.getSchool())
                            .schoolId(user.getSchoolId())
                            .build()
            );
        }
        for (PostParticipant participant : receiver_prev) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            receiver.add(
                    SimplifiedUserProfileDTO.builder()
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .department(user.getDepartment())
                            .schoolId(user.getSchoolId())
                            .school(user.getSchool())
                            .build()
            );
        }

        //tag의 head들을 전부 가져오기
        List<PostTag> tags = postTagRepository.findAllByPost_PostIdx(roomIdx);
        List<String> heads = new ArrayList<>();
        for (PostTag tag : tags) {
            heads.add(tag.getTagContent());
        }

        boolean isRequested = postParticipantRepository.existsByPost_PostIdxAndUserIdx(roomIdx,userIdx);

        return PostViewDTO.builder()
                .postIdx(post.getPostIdx())
                .title(post.getTitle())
                .groupConstraint(post.getParticipantConstraint())
                .location(post.getPlace())
                .meetingAt(Timestamp.valueOf(post.getMeetingDate()))
                .buyer(buyer)
                .receiver(receiver)
                .contents(post.getContent())
                .tagHead(heads)
                .isRequested(isRequested)
                .build();
    }

    public boolean checkIfUserParticipating(long postIdx, long userIdx) {
        System.out.println("postIdx = " + postIdx);
        System.out.println("userIdx = " + userIdx);
        return postParticipantRepository.existsByPost_PostIdxAndUserIdxAndStatus(postIdx,userIdx,"active");
    }

    @Transactional
    public void exitAppointment(long postIdx, long userIdx) throws BaseException {
        //해당 post에서 나가기
        //1. 일단 postParticipant에서 제거, 그전에 포지션부터 가져오자
        postParticipantRepository.deleteByPost_PostIdxAndUserIdx(postIdx, userIdx);
        Post post = postRepository.findPostByPostIdx(postIdx);
        Long chatRoomIdx = post.getChatRoomIdx();
        chatParticipantRepository.deleteByChatNUser(new ChatNUser(chatRoomIdx,userIdx));

        //2. 해당 user의 이탈로 만약 더이상 참여 인원이 없을시 -> post와 chatroom을 제거
        long remains = postParticipantRepository.countByPost_PostIdxAndStatus(postIdx,"active");
        if(remains == 0){
            //TODO : 제거로직 확실히 하기!!
            deleteEntirePost(postIdx);
        }
        //3. 해당 유저가 post의 주인일 경우? 아무 buyer에게 owner권한을 넘기자
        //만약 buyer가 없다면?.... 방을 터트리는게 맞지 않을까

        long remains_buyer = postParticipantRepository.countByPost_PostIdxAndStatusAndPosition(postIdx,"active","buyer");
        if(remains_buyer == 0){
            //TODO : buyer가 전부 나간 상황에서 어찌처리할지 결정 해야함 1) 그냥 방 폭파 2) 아무 receiver에게 위임
            deleteEntirePost(postIdx);
            return;
        }


        if(userIdx == post.getWriterIdx()){
            List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(postIdx,"active","BUYER");
            post.setWriterIdx(buyer_prev.get(0).getUserIdx());
            postRepository.save(post);
        }
    }

    public void kickUser(long postIdx, long kickerIdx, long kickedIdx) throws BaseException{
        long roomIdx = postRepository.findPostByPostIdx(postIdx).getChatRoomIdx();
        chatParticipantRepository.deleteByChatNUser_UserIdxAndChatNUser_ChatRoomIdx(kickedIdx,roomIdx);
        noticeRepository.save(
                Notice.builder()
                        .postIdx(0L)
                        .userIdx(kickedIdx)
                        .type("kick")
                        .content("약속에서 강퇴당했습니다")
                        .flag(0)
                        .build()
        );
    }

    @Transactional
    public void makeNewPost(MakeNewPostReqDTO makeNewPostReqDTO) throws BaseException{
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = null;
        if(makeNewPostReqDTO.getMeetingAt() != null) {
            time = LocalDateTime.parse(makeNewPostReqDTO.getMeetingAt(), formatter);
            if (time.isBefore(LocalDateTime.now())) {
                throw new BaseException(BaseResponseStatus.DATE_TIME_ERROR);
            }
        }
        //0. chatroom을 먼저 만들어야 될듯 -> 채팅방 생성
        String uuid = UUID.randomUUID().toString();
        chatRoomRepository.save(
                ChatRoom.builder()
                        .chatRoomName(uuid)
                        .build()
        );

        ChatRoom chatRoom = chatRoomRepository.findChatRoomByChatRoomName(uuid);

        //1. 일단 post부터 만들기
        postRepository.save(
                Post.builder()
                        .writerIdx(makeNewPostReqDTO.getWriterIdx())
                        .title(makeNewPostReqDTO.getTitle())
                        .place(makeNewPostReqDTO.getLocation())
                        .content(makeNewPostReqDTO.getContent())
                        .maxBuyerNum(makeNewPostReqDTO.getBuyerNum())
                        .maxReceiverNum(makeNewPostReqDTO.getReceiverNum())
                        .recruitmentStatus("active")
                        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
                        .viewCount(0)
                        .meetingDate(time)
                        .meetingType(makeNewPostReqDTO.getType())
                        .participantLimit(makeNewPostReqDTO.getReceiverNum() + makeNewPostReqDTO.getBuyerNum())
                        .participantConstraint(makeNewPostReqDTO.getConstraint())
                        .chatRoomIdx(chatRoom.getChatRoomIdx())
                        .build()
        );

        Post post = postRepository.findPostByWriterIdxAndAndChatRoomIdx(makeNewPostReqDTO.getWriterIdx(), chatRoom.getChatRoomIdx());

        //writer postParticipant만들기.. 이거 왜 안만듬ㅅㅂ
        postParticipantRepository.save(
                PostParticipant.builder()
                        .userIdx(makeNewPostReqDTO.getWriterIdx())
                        .status("active")
                        .position(makeNewPostReqDTO.getWriterPosition())
                        .post(post)
                        .build()
        );

        chatParticipantRepository.save(
                ChatParticipant.builder()
                        .chatNUser(new ChatNUser(chatRoom.getChatRoomIdx(), makeNewPostReqDTO.getWriterIdx()))
                        .status("active")
                        .build()
        );


        if(makeNewPostReqDTO.getInvitedIdx() != null) {
            //2. 이후 participant만들기 - chat하고 post둘다
            List<Long> buyers = makeNewPostReqDTO.getInvitedIdx();
            buyers.add(makeNewPostReqDTO.getWriterIdx());
            for (Long buyer : buyers) {
                if (!userRepository.existsUserByUserIdx(buyer))
                    throw new BaseException(BaseResponseStatus.INVALID_USER);
                System.out.println("chatRoom.getChatRoomIdx() = " + chatRoom.getChatRoomIdx());
                chatParticipantRepository.save(
                        ChatParticipant.builder()
                                .chatNUser(new ChatNUser(chatRoom.getChatRoomIdx(), buyer))
                                .status("active")
                                .build()
                );
                postParticipantRepository.save(
                        PostParticipant.builder()
                                .userIdx(buyer)
                                .position("buyer")
                                .status("active")
                                .post(post)
                                .build()
                );
            }
        }

        if(makeNewPostReqDTO.getTags()!=null) {
            //3. 태그들 싹 추가하기
            List<String> tags = makeNewPostReqDTO.getTags();
            for (String tag : tags) {
                postTagRepository.save(
                        PostTag.builder()
                                .tagContent(tag)
                                .post(post)
                                .build()
                );
            }
        }

    }

    public void drawbackRequest(Long userIdx, Long postIdx) throws BaseException {
       //1. 존재하지 않는지 확인
        boolean existCheck = postParticipantRepository.existsByPost_PostIdxAndUserIdx(postIdx,userIdx);
        if(!existCheck){
            throw new BaseException(BaseResponseStatus.INVALID_ACCESS_TO_APPOINTMENT);
        }
        boolean participantCheck = postParticipantRepository.existsByPost_PostIdxAndUserIdxAndStatus(postIdx,userIdx,"active");
        if(participantCheck){
            throw new BaseException(BaseResponseStatus.ALREADY_PARTICIPATED_IN_ROOM);
        }
        PostParticipant pp = postParticipantRepository.findByPost_PostIdxAndUserIdxAndStatus(postIdx,userIdx,"waiting");
        postParticipantRepository.delete(pp);
    }

    private boolean deleteEntirePost(long postIdx){
        Post post = postRepository.findPostByPostIdx(postIdx);
        //chatRoom착제 prev
        chatParticipantRepository.deleteAllParticipationInChatroom(post.getChatRoomIdx());
        chatMessageRepository.deleteAllByChatRoom_ChatRoomIdx(post.getChatRoomIdx());
        chatRepository.deleteAllByChatRoom_ChatRoomIdx(post.getChatRoomIdx());
        postPhotoRepository.deleteAllByPost(post);
        postTagRepository.deleteAllByPost(post);

        List<Vote> vr_list = voteRepository.findAllByPostIdx(postIdx);
        for (Vote vote : vr_list) {
            voteParticipatedRepository.deleteAllByVote(vote);
            voteRecordRepository.deleteAllByVoteId_VoteIdx(vote.getVoteIdx());
            voteRepository.deleteById(vote.getVoteIdx());
        }

        postParticipantRepository.deleteAllParticipantInPost(postIdx);
        postRepository.delete(post);
        return true;
    }
}



