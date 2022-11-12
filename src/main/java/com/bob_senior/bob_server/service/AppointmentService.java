package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.Chat.entity.ChatNUser;
import com.bob_senior.bob_server.domain.Chat.entity.ChatParticipant;
import com.bob_senior.bob_server.domain.Post.entity.*;
import com.bob_senior.bob_server.domain.appointment.AppointmentHeadDTO;
import com.bob_senior.bob_server.domain.appointment.AppointmentViewDTO;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import com.bob_senior.bob_server.domain.user.entity.User;
import com.bob_senior.bob_server.domain.user.UserProfile;
import com.bob_senior.bob_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    private final PostRepository postRepository;
    private final PostParticipantRepository postParticipantRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final PostTagRepository postTagRepository;

    @Autowired
    public AppointmentService(PostRepository postRepository, PostParticipantRepository postParticipantRepository, UserRepository userRepository, ChatParticipantRepository chatParticipantRepository, PostPhotoRepository postPhotoRepository, PostTagRepository postTagRepository) {
        this.postRepository = postRepository;
        this.postParticipantRepository = postParticipantRepository;
        this.userRepository = userRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.postPhotoRepository = postPhotoRepository;
        this.postTagRepository = postTagRepository;
    }




    public AppointmentViewDTO getAppointmentData(Integer postIdx) throws BaseException {
        Post post = postRepository.findPostByPostIdx(postIdx);

        List<UserProfile> buyer = new ArrayList<>();
        List<UserProfile> receiver = new ArrayList<>();

        List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsByPostUser_PostIdxAndStatusAndPosition(postIdx,"PARTICIPATE","BUYER");
        List<PostParticipant> receiver_prev = postParticipantRepository.findPostParticipantsByPostUser_PostIdxAndStatusAndPosition(postIdx,"PARTICIPATE","RECEIVER");
        for (PostParticipant participant : buyer_prev) {
            User user = userRepository.findUserByUserIdx(participant.getPostUser().getUserIdx());
            buyer.add(
                    UserProfile.builder()
                            .nickname(user.getNickName())
                            .schoolId(user.getSchoolId())
                            .isOnline(false)
                            .profileImgURL("hello")
                            .build()
                    //TODO : isOnlien이나 profileImg등을 어찌할까..
            );
        }
        for (PostParticipant participant : receiver_prev) {
            User user = userRepository.findUserByUserIdx(participant.getPostUser().getUserIdx());
            receiver.add(
                    UserProfile.builder()
                            .nickname(user.getNickName())
                            .schoolId(user.getSchoolId())
                            .isOnline(false)
                            .profileImgURL("hellp")
                            .build()
            );
        }
        return AppointmentViewDTO.builder()
                .location(post.getPlace())
                .meetingAt(post.getMeetingDate())
                .buyers(buyer)
                .receivers(receiver)
                .build();
    }




    public List<AppointmentHeadDTO> getUserWaitingAppointment(Integer userIdx, Pageable pageable) throws BaseException{
        //해당 유저의 waiting상태의 request을 전부 가져오기
        //List<AppointmentRequest> appointmentList= appointmentRequestRepository.findAllByPostUser_UserIdxAndStatus(userIdx,"WAITING",pageable).getContent();
        List<PostParticipant> participantList = postParticipantRepository.findAllByPostUser_UserIdxAndStatus(userIdx,"WAITING",pageable).getContent();

        List<AppointmentHeadDTO> data = new ArrayList<>();

        for (PostParticipant waiting : participantList) {
            Post post = postRepository.findPostByPostIdx(waiting.getPostUser().getPostIdx());

            long currNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"PARTICIPATE");

            long waitingNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"WAITING");

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());

            SimplifiedUserProfileDTO writer_simp = SimplifiedUserProfileDTO.builder()
                    .nickname(writer.getNickName())
                    .department(writer.getDepartment())
                            .schoolId(writer.getSchoolId())
                                    .school(writer.getSchool())
                                    .school(writer.getSchool())
                                            .build();

            data.add(
                    AppointmentHeadDTO.builder()
                            .title(post.getTitle())
                            .writtenAt(post.getRegisteredAt())
                            .imageURL(
                                    postPhotoRepository.findPostPhotoByPhotoId_PostIdx(post.getPostIdx()).getPhotoId().getPostPhotoUrl()
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
        return data;
    }




    public List<AppointmentHeadDTO> getUserParticipatedAppointment(Integer userIdx, Pageable pageable) throws BaseException{

        List<PostParticipant> list_participating = postParticipantRepository.findAllByPostUser_UserIdxAndStatus(userIdx,"PARTICIPATE",pageable).getContent();

        List<AppointmentHeadDTO> data = new ArrayList<>();

        for (PostParticipant participant : list_participating) {
            Post post = postRepository.findPostByPostIdx(participant.getPostUser().getPostIdx());

            long currNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"PARTICIPATE");

            long waitingNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"WAITING");

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());

            SimplifiedUserProfileDTO writer_simp = SimplifiedUserProfileDTO.builder()
                    .nickname(writer.getNickName())
                    .department(writer.getDepartment())
                    .schoolId(writer.getSchoolId())
                    .school(writer.getSchool())
                    .build();

            data.add(
                    AppointmentHeadDTO.builder()
                            .title(post.getTitle())
                            .writtenAt(post.getRegisteredAt())
                            .imageURL(
                                    postPhotoRepository.findPostPhotoByPhotoId_PostIdx(post.getPostIdx()).getPhotoId().getPostPhotoUrl()
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
        return data;
    }




    public void makeNewPostParticipation(int postIdx, int userIdx) throws BaseException{
        //1. 일단 방이 존재하는지 확인
        boolean exist = postRepository.existsByPostIdxAndRecruitmentStatus(postIdx,"ACTIVATE");
        if(!exist) throw new BaseException(BaseResponseStatus.NON_EXIST_POSTIDX);

         //2. 해당 방에 이미 참여중인지 확인 or 이미 waiting인 상태가 존재하는지 or 이미 reject된게 존재하는지 -> 그냥 participant를 보면 되네
        boolean already_participated = postParticipantRepository.existsByPostUser(new PostUser(postIdx,userIdx));
        if(already_participated) throw new BaseException(BaseResponseStatus.ALREADY_PARTICIPATED_IN_ROOM);

        //3. 이미 만료된(풀방, finished)인지 확인
        Long cur_participation_count = postParticipantRepository.countByPostUser_PostIdxAndStatus(postIdx,"PARTICIPATING");
        Post p = postRepository.findPostByPostIdx(postIdx);
        if(cur_participation_count>=p.getParticipantLimit()
                || p.getRecruitmentStatus() == "FINISHED"){
            //이미 풀방 || recruitmentStatus가 이미 완료시 더이상 참여 불가능
            throw new BaseException(BaseResponseStatus.UNABLE_TO_MAKE_REQUEST_IN_POST);
        }

        //4. 위의 verification 을 모두 통과시 participation 을 추가하기
        postParticipantRepository.save(PostParticipant.builder()
                .postUser(new PostUser(postIdx,userIdx))
                .status("WAITING")
                .build());
    }




    public boolean isOwnerOfPost(Integer userIdx, Integer postIdx) {
        //내가 해당 게시글의 owner인지 확인
        Post post = postRepository.findPostByPostIdx(postIdx);
        return post.getWriterIdx() == userIdx;



    }




    public boolean isPostExist(Integer postIdx) {
        return postRepository.existsByPostIdxAndRecruitmentStatus(postIdx,"ACTIVATE");
    }




    public List<SimplifiedUserProfileDTO> getAllRequestInPost(Integer postIdx, Pageable pageable) {
        //모든 request의 head를 가져온다.
        List<PostParticipant> list = postParticipantRepository.findAllByPostUser_PostIdxAndStatus(postIdx,"WAITING",pageable).getContent();
        List<SimplifiedUserProfileDTO> data = new ArrayList<>();
        for (PostParticipant participant : list) {
            User user = userRepository.findUserByUserIdx(participant.getPostUser().getUserIdx());
            data.add(SimplifiedUserProfileDTO.builder()
                    .nickname(user.getNickName())
                    .department(user.getDepartment())
                    .school(user.getSchool())
                    .schoolId(user.getSchoolId())
                    .build());
        }
        return data;
    }




    public void determineRequestStatus(Integer postIdx, Integer requesterIdx, boolean accept) throws BaseException{
        //1. 해당 request가 존재했는지 확인하는게 우선
        boolean isExist = postParticipantRepository.existsByPostUser(new PostUser(postIdx,requesterIdx));
        if(!isExist) throw new BaseException(BaseResponseStatus.NON_EXIST_POST_PARTICIPATION);
        Post post = postRepository.findPostByPostIdx(postIdx);
        int total = post.getParticipantLimit();
        long curr = postParticipantRepository.countByPostUser_PostIdxAndStatus(postIdx,"PARTICIPATE");
        if(total - curr <=0){
            //더이상 참여 불가능 ->
            throw new BaseException(BaseResponseStatus.UNABLE_TO_PARTICIPATE_IN_POST);
        }
        //2. 존재할 경우 -> accept에 따라 다르게 처리
        if(accept){
            //true -> 해당 request를 받아들일 경우
            //1. 일단 postParticipation을 participate로 변경
            postParticipantRepository.changePostParticipationStatus("PARTICIPATE",postIdx,requesterIdx);
            //2. 그 후 해당 post의 chatroom에 추가 ->
            chatParticipantRepository.save(
                    ChatParticipant.builder()
                            .chatNUser(new ChatNUser(postIdx,requesterIdx))
                            .status("A")
                            .lastRead(null)
                            .build()
            );
            changeRecruitmentStatusIfFull(post, total, curr);
        }
        else{
            //false일 경우 -> requst를 거절
            postParticipantRepository.changePostParticipationStatus("REJECT",postIdx,requesterIdx);
        }
    }




    //해당 유저가 접근가능한 약속 리스트 전부 가져오기
    public List<AppointmentHeadDTO> getAvailableAppointmentList(Integer userIdx, Pageable pageable) {
        //1. 유저의 소속정보 가져오기(school, dep, year)
        User user = userRepository.findUserByUserIdx(userIdx);
        List<Post> posts = postRepository.getAllThatCanParticipant(user.getDepartment()).getContent();
        List<AppointmentHeadDTO> data = new ArrayList<>();
        for (Post post : posts) {

            long currNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"PARTICIPATE");

            long waitingNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"WAITING");

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());
            data.add(
                    AppointmentHeadDTO.builder()
                            .title(post.getTitle())
                            .imageURL(
                                    postPhotoRepository.findPostPhotoByPhotoId_PostIdx(post.getPostIdx()).getPhotoId().getPostPhotoUrl()
                            )
                            .writer(
                                    SimplifiedUserProfileDTO.builder()
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
                            .build()
            );
        }
    return data;
    }




    //해당 postIdx로 uuid의 유저를 초대
    public void inviteUserByUUID(String invitedUUID, Integer postIdx) throws BaseException{
        //일단 postIdx에 참여 가능여부를 확인
        Post post = postRepository.findPostByPostIdx(postIdx);
        int total = post.getParticipantLimit();
        //현재 참여중인 user의 수
        long curr = postParticipantRepository.countByPostUser_PostIdxAndStatus(postIdx,"PARTICIPATE");

        //더이상 참여가 불가능한 경우
        if(total - curr <=0){
            //더이상 참여가 불가능
            throw new BaseException(BaseResponseStatus.UNABLE_TO_PARTICIPATE_IN_POST);
        }
        //uuid로 일단 유저 가져오기
        User user = userRepository.findByUuid(invitedUUID);

        //이미 참여중인 경우
        if(postParticipantRepository.existsByPostUserAndAndStatus(new PostUser(postIdx,user.getUserIdx()),"PARTICIPATE")){
            throw new BaseException(BaseResponseStatus.ALREADY_PARTICIPATED_IN_ROOM);
        }

        //참여시키기 1. postParticipant에 추가 + chatParticipant에 추가
        postParticipantRepository.save(PostParticipant.builder()
                .postUser(new PostUser(postIdx,user.getUserIdx()))
                .status("PARTICIPATE")
                .build()
        );
        chatParticipantRepository.save(
                ChatParticipant.builder()
                        .chatNUser(new ChatNUser(postIdx, user.getUserIdx()))
                        .status("A")
                        .build()
        );

        //참여 이후 만약 total과 같아질시 recruitment status를 "finish"로 바꾸기
        changeRecruitmentStatusIfFull(post, total, curr);
    }

    private void changeRecruitmentStatusIfFull(Post post, int total, long curr) {
        if(curr +1 == total){
            post.setRecruitmentStatus("finish");
            postRepository.save(post);
        }
    }



    //해당 string으로 검색하기 -> 타이틀 검색?
    public List<AppointmentHeadDTO> searchByStringInTitle(Integer userIdx,String searchString, Pageable pageable) throws BaseException {
        List<AppointmentHeadDTO> heads = new ArrayList<>();
        String dep = userRepository.findUserByUserIdx(userIdx).getDepartment();
        List<Post> list = postRepository.searchAllParticipantThatCanParticipant(dep,searchString,pageable).getContent();
        for (Post post : list) {
            long currNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"PARTICIPATE");

            long waitingNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"WAITING");

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());
            heads.add(
                    AppointmentHeadDTO.builder()
                            .title(post.getTitle())
                            .writtenAt(post.getRegisteredAt())
                            .imageURL(
                                    postPhotoRepository.findPostPhotoByPhotoId_PostIdx(post.getPostIdx()).getPhotoId().getPostPhotoUrl()
                            )
                            .writer(
                                    SimplifiedUserProfileDTO.builder()
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
                            .build()
            );
        }
        return heads;
    }

    public List<AppointmentHeadDTO> searchByTag(Integer userIdx, String tag, Pageable pageable) throws BaseException{
        //1. tag의 존재 여부
        boolean isExist = postTagRepository.existsByTag_TagContent(tag);
        if(!isExist) throw new BaseException(BaseResponseStatus.TAG_DOES_NOT_EXIST);

        //2. 해당 태그를 가지는 모든 post를 가져와서 가공
        List<AppointmentHeadDTO> heads = new ArrayList<>();
        String dep = userRepository.findUserByUserIdx(userIdx).getDepartment();
        List<PostTag> list = postTagRepository.searchTagThatCanParticipate(tag,dep,pageable).getContent();
        for (PostTag pt : list) {
            Post post = pt.getPost();
            long currNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"PARTICIPATE");

            long waitingNum = postParticipantRepository.countByPostUser_PostIdxAndStatus(post.getPostIdx(),"WAITING");

            User writer = userRepository.findUserByUserIdx(post.getWriterIdx());
            heads.add(
                    AppointmentHeadDTO.builder()
                            .title(post.getTitle())
                            .writtenAt(post.getRegisteredAt())
                            .imageURL(
                                    postPhotoRepository.findPostPhotoByPhotoId_PostIdx(post.getPostIdx()).getPhotoId().getPostPhotoUrl()
                            )
                            .writer(
                                    SimplifiedUserProfileDTO.builder()
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
                            .build()
            );
        }
        return heads;
    }

    public PostViewDTO getPostData(Integer roomIdx,Integer userIdx) throws BaseException{
        //해당 post의 데이터 싸그리 가져오기
        Post post = postRepository.findPostByPostIdx(roomIdx);

        List<SimplifiedUserProfileDTO> buyer = new ArrayList<>();
        List<SimplifiedUserProfileDTO> receiver = new ArrayList<>();

        List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsByPostUser_PostIdxAndStatusAndPosition(roomIdx,"PARTICIPATE","BUYER");
        List<PostParticipant> receiver_prev = postParticipantRepository.findPostParticipantsByPostUser_PostIdxAndStatusAndPosition(roomIdx,"PARTICIPATE","RECEIVER");
        for (PostParticipant participant : buyer_prev) {
            User user = userRepository.findUserByUserIdx(participant.getPostUser().getUserIdx());
            buyer.add(
                    SimplifiedUserProfileDTO.builder()
                            .nickname(user.getNickName())
                            .department(user.getDepartment())
                            .school(user.getSchool())
                            .schoolId(user.getSchoolId())
                            .build()
            );
        }
        for (PostParticipant participant : receiver_prev) {
            User user = userRepository.findUserByUserIdx(participant.getPostUser().getUserIdx());
            receiver.add(
                    SimplifiedUserProfileDTO.builder()
                            .nickname(user.getNickName())
                            .department(user.getDepartment())
                            .schoolId(user.getSchoolId())
                            .school(user.getSchool())
                            .build()
            );
        }

        //tag의 head들을 전부 가져오기
        List<PostTag> tags = postTagRepository.findAllByTag_PostIdx(roomIdx);
        List<String> heads = new ArrayList<>();
        for (PostTag tag : tags) {
            heads.add(tag.getTag().getTagContent());
        }

        boolean isRequested = postParticipantRepository.existsByPostUser(new PostUser(roomIdx,userIdx));

        return PostViewDTO.builder()
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
}


