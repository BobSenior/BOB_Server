package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.appointment.MakeNewPostReqDTO;
import com.bob_senior.bob_server.domain.appointment.RequestData;
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
import java.util.StringTokenizer;
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
        String location = null;
        String lat = null;
        String longt = null;
        if(post.getPlace() != null) {
            StringTokenizer st = new StringTokenizer(post.getPlace(), "$");
            location = st.nextToken();
            lat = st.nextToken();
            longt = st.nextToken();
        }
        List<SimplifiedUserProfileDTO> buyer = new ArrayList<>();
        List<SimplifiedUserProfileDTO> receiver = new ArrayList<>();

        List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(postIdx,"active","buyer");
        List<PostParticipant> receiver_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(postIdx,"active","receiver");
        for (PostParticipant participant : buyer_prev) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            buyer.add(
                    SimplifiedUserProfileDTO.builder()
                            .uuid(user.getUuid())
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .schoolId(user.getSchoolId().substring(2,4))
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
                            .uuid(user.getUuid())
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .schoolId(user.getSchoolId().substring(2,4))
                            .isOnline(false)
                            .school(user.getSchool())
                            .department(user.getDepartment())
                            .build()
            );
        }


        Vote vote = voteRepository.getVoteByPostIdxAndIsActivated(postIdx,1);

        List<ShownVoteRecord> records = new ArrayList<>();
        if(vote!=null) {

            List<VoteRecord> vr = voteRecordRepository.findAllByVoteId_VoteIdx(vote.getVoteIdx());

            for (VoteRecord voteRecord : vr) {
                records.add(
                        ShownVoteRecord.builder().id(voteRecord.getVoteId().getChoiceIdx()).content(voteRecord.getVoteContent()).count(voteRecord.getCount()).build()
                );
            }
        }

        //?????? post??? ?????? ?????? notice??? ???????????????
        noticeRepository.disablePostRelatedNotice(postIdx,userIdx);

        if(voteRepository.findVoteByPostIdxAndIsActivated(postIdx,1) == null){
            return AppointmentViewDTO.builder()
                    .writerIdx(post.getWriterIdx())
                    .constraint(post.getParticipantConstraint())
                    .title(post.getTitle())
                    .postIdx(post.getPostIdx())
                    .location(location)
                    .latitude(lat)
                    .longitude(longt)
                    .maxBuyerNum(post.getMaxBuyerNum())
                    .maxReceiverNum(post.getMaxReceiverNum())
                    .meetingAt(post.getMeetingDate() == null?null : post.getMeetingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .fixVote(false)
                    .buyers(buyer)
                    .receivers(receiver)
                    .chatRoomIdx(post.getChatRoomIdx())
                    .type(post.getMeetingType())
                    .build();
        }

        return AppointmentViewDTO.builder()
                .writerIdx(post.getWriterIdx())
                .constraint(post.getParticipantConstraint())
                .voteIdx(voteRepository.findVoteByPostIdxAndIsActivated(postIdx,1).getVoteIdx())
                .title(post.getTitle())
                .postIdx(post.getPostIdx())
                .location(location)
                .latitude(lat)
                .longitude(longt)
                .maxBuyerNum(post.getMaxBuyerNum())
                .maxReceiverNum(post.getMaxReceiverNum())
                .meetingAt(post.getMeetingDate() == null ? null : post.getMeetingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .buyers(buyer)
                .receivers(receiver)
                .voteTitle(voteRepository.findVoteByPostIdxAndIsActivated(postIdx,1).getTitle())
                .fixVote(!voteRepository.findVoteByPostIdxAndIsActivated(postIdx,1).getVoteType().equals("NORMAL"))
                .records(records)
                .voteOwnerIdx(voteRepository.findVoteByPostIdxAndIsActivated(postIdx,1).getCreatorIdx())
                .maxNum(buyer.size() + receiver.size())
                .alreadyVoted(voteParticipatedRepository.existsVoteParticipatedByUserIdxAndVote_VoteIdx(userIdx,vote.getVoteIdx()))
                .chatRoomIdx(post.getChatRoomIdx())
                .type(post.getMeetingType())
                .build();
    }




    public List<AppointmentHeadDTO> getUserWaitingAppointment(Long userIdx, Pageable pageable) throws BaseException{
        //?????? ????????? waiting????????? request??? ?????? ????????????
        //List<AppointmentRequest> appointmentList= appointmentRequestRepository.findAllByPostUser_UserIdxAndStatus(userIdx,"WAITING",pageable).getContent();
        List<PostParticipant> participantList = postParticipantRepository.findAllByUserIdxAndStatus(userIdx,"WAITING",pageable).getContent();

        List<AppointmentHeadDTO> data = new ArrayList<>();

        for (PostParticipant waiting : participantList) {
            Post post = postRepository.findPostByPostIdx(waiting.getPost().getPostIdx());

            String location_raw = post.getPlace();
            String location_real = null;
            if(location_raw!=null){
                StringTokenizer st = new StringTokenizer(location_raw,"$");
                location_real = st.nextToken();
            }

            long currNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"active");

            long waitingNum = postParticipantRepository.countByPost_PostIdxAndStatus(post.getPostIdx(),"waiting");

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());

            List<PostTag> tags = postTagRepository.findAllByPost_PostIdx(post.getPostIdx());
            List<String> heads = new ArrayList<>();
            for (PostTag tag : tags) {
                heads.add(tag.getTagContent());
            }



            SimplifiedUserProfileDTO writer_simp = SimplifiedUserProfileDTO.builder()
                    .uuid(writer.getUuid())
                    .userIdx(writer.getUserIdx())
                    .nickname(writer.getNickName())
                    .department(writer.getDepartment())
                            .schoolId(writer.getSchoolId().substring(2,4))
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
                            .location(location_real)
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
                    .uuid(writer.getUuid())
                    .userIdx(writer.getUserIdx())
                    .nickname(writer.getNickName())
                    .department(writer.getDepartment())
                    .schoolId(writer.getSchoolId().substring(2,4))
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

        //??????????????? ?????? post??? ???????????? notice??? ?????????.
        return data;
    }




    @Transactional
    public void makeNewPostParticipation(Long postIdx, Long userIdx,String position) throws BaseException{
        //1. ?????? ?????? ??????????????? ??????
        boolean exist = postRepository.existsByPostIdxAndRecruitmentStatus(postIdx,"active");
        if(!exist) throw new BaseException(BaseResponseStatus.NON_EXIST_POSTIDX);

         //2. ?????? ?????? ?????? ??????????????? ?????? or ?????? waiting??? ????????? ??????????????? or ?????? reject?????? ??????????????? -> ?????? participant??? ?????? ??????
        boolean already_participated = postParticipantRepository.existsByPost_PostIdxAndUserIdx(postIdx,userIdx);
        if(already_participated) throw new BaseException(BaseResponseStatus.ALREADY_PARTICIPATED_IN_ROOM);

        //3. ?????? ?????????(??????, finished)?????? ?????? -> ??????????????? ????????????..
        Long cur_participation_count = postParticipantRepository.countByPost_PostIdxAndPositionAndStatus(postIdx,position,"active");
        Post p = postRepository.findPostByPostIdx(postIdx);
        if(position.equals("buyer")){
            if(cur_participation_count>=p.getMaxBuyerNum()
                    || p.getRecruitmentStatus() == "finish"){
                //?????? ?????? || recruitmentStatus??? ?????? ????????? ????????? ?????? ?????????
                throw new BaseException(BaseResponseStatus.UNABLE_TO_MAKE_REQUEST_IN_POST);
            }
        }
        else{
            if(cur_participation_count>=p.getMaxReceiverNum()
                    || p.getRecruitmentStatus() == "finish"){
                //?????? ?????? || recruitmentStatus??? ?????? ????????? ????????? ?????? ?????????
                throw new BaseException(BaseResponseStatus.UNABLE_TO_MAKE_REQUEST_IN_POST);
            }
        }
        //4. ?????? verification ??? ?????? ????????? participation ??? ????????????
        postParticipantRepository.save(PostParticipant.builder()
                .userIdx(userIdx)
                .post(postRepository.findPostByPostIdx(postIdx))
                .status("waiting")
                .position(position)
                .build());

        //5. ?????? ???????????? notice????????? ????????? ??????
        noticeRepository.save(
                Notice.builder()
                        .userIdx(postRepository.findPostByPostIdx(postIdx).getWriterIdx())
                        .postIdx(postIdx)
                        .flag(0)
                        .content(postIdx+"??? ??????????????? ????????????")
                        .type("PAIRequest")
                       .build()
        );
    }




    public boolean isOwnerOfPost(Long userIdx, Long postIdx) {
        //?????? ?????? ???????????? owner?????? ??????
        Post post = postRepository.findPostByPostIdx(postIdx);
        System.out.println("post.getWriterIdx() = " + post.getWriterIdx());
        System.out.println("userIdx = " + userIdx);
        return post.getWriterIdx() == userIdx;
    }




    public boolean isPostExist(Long postIdx) {
        return postRepository.existsByPostIdx(postIdx);
    }




    public List<RequestData> getAllRequestInPost(Long userIdx, Long postIdx, Pageable pageable) {
        //?????? request??? head??? ????????????.
        List<PostParticipant> list = postParticipantRepository.findAllByPost_PostIdxAndStatus(postIdx,"waiting",pageable).getContent();
        List<RequestData> data = new ArrayList<>();
        for (PostParticipant participant : list) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            data.add(
                    RequestData.builder()
                                    .position(participant.getPosition())
                                            .simp(
                                                    SimplifiedUserProfileDTO.builder()
                                                            .uuid(user.getUuid())
                                                            .userIdx(user.getUserIdx())
                                                            .nickname(user.getNickName())
                                                            .department(user.getDepartment())
                                                            .school(user.getSchool())
                                                            .schoolId(user.getSchoolId().substring(2,4))
                                                            .build())     .build()
                                            );
        }
        // request?????? ?????? flag????????????
        noticeRepository.disableFriendRequestNotice("request",userIdx);
        return data;
    }




    @Transactional
    public void determineRequestStatus(Long postIdx, Long requesterIdx, boolean accept) throws BaseException{
        //1. ?????? request??? ??????????????? ??????????????? ??????
        boolean isExist = postParticipantRepository.existsByPost_PostIdxAndUserIdx(postIdx,requesterIdx);
        if(!isExist) throw new BaseException(BaseResponseStatus.NON_EXIST_POST_PARTICIPATION);
        Post post = postRepository.findPostByPostIdx(postIdx);
        int total = post.getParticipantLimit();
        long curr = postParticipantRepository.countByPost_PostIdxAndStatus(postIdx,"active");
        if(total - curr <=0){
            //????????? ?????? ????????? ->
            throw new BaseException(BaseResponseStatus.UNABLE_TO_PARTICIPATE_IN_POST);
        }
        //2. ????????? ?????? -> accept??? ?????? ????????? ??????
        String result = "";
        String type="";
        if(accept){
            //true -> ?????? request??? ???????????? ??????
            //1. ?????? postParticipation??? participate??? ??????
            postParticipantRepository.changePostParticipationStatus("active",postIdx,requesterIdx);
            //2. ??? ??? ?????? post??? chatroom??? ?????? ->
            chatParticipantRepository.save(
                    ChatParticipant.builder()
                            .chatNUser(new ChatNUser(post.getChatRoomIdx(),requesterIdx))
                            .status("active")
                            .lastRead(null)
                            .build()
            );
            changeRecruitmentStatusIfFull(post, total, curr);
            type="accept";
            result = "??????????????? ?????????????????????";
            long count = postParticipantRepository.countByPost_PostIdxAndStatus(postIdx,"active");
            if(count == post.getParticipantLimit()){
                post.setRecruitmentStatus("finish");
                postRepository.save(post);
            }
        }
        else{
            //false??? ?????? -> requst??? ??????
            postParticipantRepository.changePostParticipationStatus("reject",postIdx,requesterIdx);
            result = "??????????????? ?????????????????????";
            type="PAIReject";
            noticeRepository.save(
                    Notice.builder()
                            .postIdx(postIdx)
                            .userIdx(requesterIdx)
                            .flag(0)
                            .type("PAIReject")
                            .content(result)
                            .build()
            );
        }
    }





    //?????? ????????? ??????????????? ?????? ????????? ?????? ????????????
    public List<AppointmentHeadDTO> getAvailableAppointmentList(Long userIdx, Pageable pageable) {
        //1. ????????? ???????????? ????????????(school, dep, year)
        User user = userRepository.findUserByUserIdx(userIdx);
        LocalDateTime now = LocalDateTime.now();
        List<Post> posts = postRepository.getAllThatCanParticipant("active",user.getDepartment(),now,userIdx,pageable).getContent();
        System.out.println("posts = " + posts + pageable);
        List<AppointmentHeadDTO> data = new ArrayList<>();
        for (Post post : posts) {

            String placeRaw = post.getPlace();
            String location_read = null;
            if(placeRaw != null) {
                StringTokenizer st = new StringTokenizer(placeRaw, "$");
                location_read = st.nextToken();
            }
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
                                            .schoolId(writer.getSchoolId().substring(2,4))
                                            .school(writer.getSchool())
                                            .uuid(writer.getUuid())
                                            .build()
                            )
                            .location(location_read)
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
    //?????? postIdx??? uuid??? ????????? ??????
    public void inviteUserByUUID(String invitedUUID, Long postIdx,String position) throws BaseException{
        //?????? postIdx??? ?????? ??????????????? ??????
        Post post = postRepository.findPostByPostIdx(postIdx);
        int total = 0;
        if(position.equals("buyer")){
            total = post.getMaxBuyerNum();
        }
        else total = post.getMaxReceiverNum();

        //?????? ???????????? user??? ???
        long curr = postParticipantRepository.countByPost_PostIdxAndPositionAndStatus(postIdx,position,"active").intValue();

        //????????? ????????? ???????????? ??????
        if(total - curr <=0){
            //????????? ????????? ?????????
            throw new BaseException(BaseResponseStatus.UNABLE_TO_PARTICIPATE_IN_POST);
        }
        //uuid??? ?????? ?????? ????????????
        if(!userRepository.existsByUuid(invitedUUID)){
            throw new BaseException(BaseResponseStatus.INVALID_UUID_FOR_USER);
        }

        User user = userRepository.findByUuid(invitedUUID);

        //?????? ???????????? ??????
        if(postParticipantRepository.existsByPost_PostIdxAndUserIdxAndStatus(postIdx,user.getUserIdx(),"active")){
            throw new BaseException(BaseResponseStatus.ALREADY_PARTICIPATED_IN_ROOM);
        }

        //??????????????? 1. postParticipant??? ?????? + chatParticipant??? ??????
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

        //?????? ?????? ?????? total??? ???????????? recruitment status??? "finish"??? ?????????
        changeRecruitmentStatusIfFull(post, total, curr);
    }

    private void changeRecruitmentStatusIfFull(Post post, int total, long curr) {
        if(curr +1 == total){
            post.setRecruitmentStatus("finish");
            postRepository.save(post);
        }
    }



    //?????? string?????? ???????????? -> ????????? ???????
    public List<AppointmentHeadDTO> searchByStringInTitle(Long userIdx,String searchString, Pageable pageable) throws BaseException {
        List<AppointmentHeadDTO> heads = new ArrayList<>();
        User user = userRepository.findUserByUserIdx(userIdx);
        LocalDateTime now = LocalDateTime.now();
        String dep = userRepository.findUserByUserIdx(userIdx).getDepartment();
        //List<Post> list = postRepository.searchAllParticipantThatCanParticipant("active",dep,searchString,pageable).getContent();
        List<Post> list = postRepository.getAllParticipantThatCanParticipant(dep,searchString,now,userIdx,pageable).getContent();
        System.out.println("list = " + list);
        for (Post post : list) {
            String location_raw = post.getPlace();
            String location_real = null;
            if(location_raw != null) {
                StringTokenizer st = new StringTokenizer(location_raw, "$");
                location_real = st.nextToken();
            }
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
                                            .uuid(writer.getUuid())
                                            .userIdx(writer.getUserIdx())
                                            .nickname(writer.getNickName())
                                            .department(writer.getDepartment())
                                            .schoolId(writer.getSchoolId().substring(2,4))
                                            .school(writer.getSchool())
                                            .build()
                            )
                            .location(location_real)
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
        //1. tag??? ?????? ??????
        boolean isExist = postTagRepository.existsByTagContent(tag);
        if(!isExist) throw new BaseException(BaseResponseStatus.TAG_DOES_NOT_EXIST);

        //2. ?????? ????????? ????????? ?????? post??? ???????????? ??????
        List<AppointmentHeadDTO> heads = new ArrayList<>();
        String dep = userRepository.findUserByUserIdx(userIdx).getDepartment();
        List<PostTag> list = postTagRepository.searchTagThatCanParticipate(tag,dep,pageable).getContent();
        for (PostTag pt : list) {
            Post post = pt.getPost();
            String location_raw = post.getPlace();
            StringTokenizer st = new StringTokenizer(location_raw,"$");
            String location_real = st.nextToken();
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
                                            .uuid(writer.getUuid())
                                            .userIdx(writer.getUserIdx())
                                            .nickname(writer.getNickName())
                                            .department(writer.getDepartment())
                                            .schoolId(writer.getSchoolId().substring(2,4))
                                            .school(writer.getSchool())
                                            .build()
                            )
                            .location(location_real)
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
        //?????? post??? ????????? ????????? ????????????
        Post post = postRepository.findPostByPostIdx(roomIdx);
        String location_raw = post.getPlace();
        String location_real=null;
        String latitude= null;
        String longitude = null;
        if(location_raw!=null) {
            StringTokenizer st = new StringTokenizer(location_raw, "$");
            location_real = st.nextToken();
            latitude = st.nextToken();
            longitude = st.nextToken();
        }

        List<SimplifiedUserProfileDTO> buyer = new ArrayList<>();
        List<SimplifiedUserProfileDTO> receiver = new ArrayList<>();

        List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(roomIdx,"active","buyer");
        List<PostParticipant> receiver_prev = postParticipantRepository.findPostParticipantsByPost_PostIdxAndStatusAndPosition(roomIdx,"active","receiver");
        for (PostParticipant participant : buyer_prev) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            buyer.add(
                    SimplifiedUserProfileDTO.builder()
                            .uuid(user.getUuid())
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .department(user.getDepartment())
                            .school(user.getSchool())
                            .schoolId(user.getSchoolId().substring(2,4))
                            .build()
            );
        }
        for (PostParticipant participant : receiver_prev) {
            User user = userRepository.findUserByUserIdx(participant.getUserIdx());
            receiver.add(
                    SimplifiedUserProfileDTO.builder()
                            .uuid(user.getUuid())
                            .userIdx(user.getUserIdx())
                            .nickname(user.getNickName())
                            .department(user.getDepartment())
                            .schoolId(user.getSchoolId().substring(2,4)) //20 18 6286
                            .school(user.getSchool())
                            .build()
            );
        }

        //tag??? head?????? ?????? ????????????
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
                .location(location_real)
                .latitude(latitude)
                .longitude(longitude)
                .meetingAt(post.getMeetingDate()==null?null:Timestamp.valueOf(post.getMeetingDate()))
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
        System.out.println("hello = " + postIdx);
        //?????? post?????? ?????????
        //1. ?????? postParticipant?????? ??????, ????????? ??????????????? ????????????
        postParticipantRepository.deleteByPost_PostIdxAndUserIdx(postIdx, userIdx);
        Post post = postRepository.findPostByPostIdx(postIdx);
        Long chatRoomIdx = post.getChatRoomIdx();
        chatParticipantRepository.deleteByChatNUser(new ChatNUser(chatRoomIdx,userIdx));
        if(post.getRecruitmentStatus().equals("finish")){
            post.setRecruitmentStatus("active");
            postRepository.save(post);
        }

        //2. ?????? user??? ????????? ?????? ????????? ?????? ????????? ????????? -> post??? chatroom??? ??????
        long remains = postParticipantRepository.countByPost_PostIdxAndStatus(postIdx,"active");
        if(remains == 0){
            //TODO : ???????????? ????????? ??????!!
            deleteEntirePost(postIdx);
            return;
        }
        //3. ?????? ????????? post??? ????????? ??????? ?????? buyer?????? owner????????? ?????????
        //?????? buyer??? ??????????.... ?????? ??????????????? ?????? ?????????

        long remains_buyer = postParticipantRepository.countByPost_PostIdxAndStatusAndPosition(postIdx,"active","buyer");
        if(remains_buyer == 0){
            //TODO : buyer??? ?????? ?????? ???????????? ?????????????????? ?????? ????????? 1) ?????? ??? ?????? 2) ?????? receiver?????? ??????
            deleteEntirePost(post.getPostIdx());
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
        postParticipantRepository.deleteByPost_PostIdxAndUserIdx(postIdx,kickedIdx);
        Post post = postRepository.findPostByPostIdx(postIdx);
        if(post.getRecruitmentStatus().equals("finish")){
            post.setRecruitmentStatus("active");
            postRepository.save(post);
        }
        //????????? record?????? ?????? ?????????
        voteParticipatedRepository.deleteVoteParticipatedByUserIdxAndVote(kickedIdx,voteRepository.getVoteByPostIdxAndIsActivated(postIdx,1));
    }

    @Transactional
    public long makeNewPost(MakeNewPostReqDTO makeNewPostReqDTO) throws BaseException{

        //??????
        //????????? 1?????? HH??? ????????? ???????????????
        //?????? ????????? 12??????????????? ??????
        //01:45??? ?????????????????????????
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime time = null;
        if(makeNewPostReqDTO.getMeetingAt() != null) {
            time = LocalDateTime.parse(makeNewPostReqDTO.getMeetingAt(), formatter);
            if (time.isBefore(LocalDateTime.now())) {
                throw new BaseException(BaseResponseStatus.DATE_TIME_ERROR);
            }
        }
        //0. chatroom??? ?????? ???????????? ?????? -> ????????? ??????
        String uuid = UUID.randomUUID().toString();
        chatRoomRepository.save(
                ChatRoom.builder()
                        .chatRoomName(uuid)
                        .build()
        );

        ChatRoom chatRoom = chatRoomRepository.findChatRoomByChatRoomName(uuid);

        //1. ?????? post?????? ?????????
        long datum = postRepository.save(
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
        ).getPostIdx();

        Post post = postRepository.findPostByWriterIdxAndAndChatRoomIdx(makeNewPostReqDTO.getWriterIdx(), chatRoom.getChatRoomIdx());

        //writer postParticipant?????????.. ?????? ??? ???????????????
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
            //2. ?????? participant????????? - chat?????? post??????
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
            //3. ????????? ??? ????????????
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
        return datum;
    }

    public void drawbackRequest(Long userIdx, Long postIdx) throws BaseException {
       //1. ???????????? ????????? ??????
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
        System.out.println("postIdxssss = " + postIdx);
        Post post = postRepository.findPostByPostIdx(postIdx);
        System.out.println("post = " + post);
        //chatRoom?????? prev
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



