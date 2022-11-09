package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.Chat.ChatNUser;
import com.bob_senior.bob_server.domain.Chat.ChatParticipant;
import com.bob_senior.bob_server.domain.Post.Post;
import com.bob_senior.bob_server.domain.Post.PostUser;
import com.bob_senior.bob_server.domain.Post.PostParticipant;
import com.bob_senior.bob_server.domain.appointment.AppointmentHeadDTO;
import com.bob_senior.bob_server.domain.appointment.AppointmentRequest;
import com.bob_senior.bob_server.domain.appointment.AppointmentViewDTO;
import com.bob_senior.bob_server.domain.appointment.RequestHeadDTO;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.user.SimplifiedUserProfileDTO;
import com.bob_senior.bob_server.domain.user.User;
import com.bob_senior.bob_server.domain.user.UserProfile;
import com.bob_senior.bob_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    private final PostRepository postRepository;
    private final PostParticipantRepository postParticipantRepository;
    private final UserRepository userRepository;
    private final AppointmentRequestRepository appointmentRequestRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Autowired
    public AppointmentService(PostRepository postRepository, PostParticipantRepository postParticipantRepository, UserRepository userRepository, AppointmentRequestRepository appointmentRequestRepository, ChatParticipantRepository chatParticipantRepository) {
        this.postRepository = postRepository;
        this.postParticipantRepository = postParticipantRepository;
        this.userRepository = userRepository;
        this.appointmentRequestRepository = appointmentRequestRepository;
        this.chatParticipantRepository = chatParticipantRepository;
    }

    public AppointmentViewDTO getAppointmentData(Integer postIdx) throws BaseException {
        Post post = postRepository.findPostByPostIdx(postIdx);

        List<UserProfile> buyer = new ArrayList<>();
        List<UserProfile> receiver = new ArrayList<>();

        List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsById_PostIdxAndStatus(postIdx,"BUYER");
        List<PostParticipant> receiver_prev = postParticipantRepository.findPostParticipantsById_PostIdxAndStatus(postIdx,"RECEIVER");
        for (PostParticipant participant : buyer_prev) {
            User user = userRepository.findUserByUserIdx(participant.getId().getUserIdx());
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
            User user = userRepository.findUserByUserIdx(participant.getId().getUserIdx());
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
        List<AppointmentRequest> appointmentList= appointmentRequestRepository.findAllByPostAndUser_UserIdxAndStatus(userIdx,"WAITING",pageable).getContent();

        List<AppointmentHeadDTO> data = new ArrayList<>();

        for (AppointmentRequest waiting : appointmentList) {
            Post post = postRepository.findPostByPostIdx(waiting.getPostUser().getPostIdx());

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
                            .imageUrl(post.getImageURL())
                            .writer(writer_simp)
                            .location(post.getPlace())
                            .meetingAt(post.getMeetingDate())
                            .type(post.getMeetingType())
                            .status(post.getRecruitmentStatus())
                            .build()
            );
        }
        return data;
    }

    public List<AppointmentHeadDTO> getUserParticipatedAppointment(Integer userIdx, Pageable pageable) throws BaseException{

        List<ChatParticipant> list_participated = chatParticipantRepository.findAllById_ChatParticipantIdx(userIdx,pageable).getContent();

        List<AppointmentHeadDTO> data = new ArrayList<>();

        for (ChatParticipant participant : list_participated) {
            Post post = postRepository.findPostByPostIdx(participant.getId().getChatRoomIdx());

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
                            .imageUrl(post.getImageURL())
                            .writer(writer_simp)
                            .location(post.getPlace())
                            .meetingAt(post.getMeetingDate())
                            .type(post.getMeetingType())
                            .status(post.getRecruitmentStatus())
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
        boolean already_participated = postParticipantRepository.existsById(new PostUser(postIdx,userIdx));
        if(already_participated) throw new BaseException(BaseResponseStatus.ALREADY_PARTICIPATED_IN_ROOM);

        //3. 이미 만료된(풀방, finished)인지 확인
        Long cur_participation_count = postParticipantRepository.countById_PostIdxAndStatus(postIdx,"PARTICIPATING");
        Post p = postRepository.findPostByPostIdx(postIdx);
        if(cur_participation_count>=p.getParticipantLimit()
                || p.getRecruitmentStatus() == "FINISHED"){
            //이미 풀방 || recruitmentStatus가 이미 완료시 더이상 참여 불가능
            throw new BaseException(BaseResponseStatus.UNABLE_TO_MAKE_REQUEST_IN_POST);
        }

        //4. 위의 verification 을 모두 통과시 participation 을 추가하기
        postParticipantRepository.save(PostParticipant.builder()
                .id(new PostUser(postIdx,userIdx))
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
        List<PostParticipant> list = postParticipantRepository.findAllById_PostIdxAndStatus(postIdx,"WAITING",pageable).getContent();
        List<SimplifiedUserProfileDTO> data = new ArrayList<>();
        for (PostParticipant participant : list) {
            User user = userRepository.findUserByUserIdx(participant.getId().getUserIdx());
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
        boolean isExist = postParticipantRepository.existsById(new PostUser(postIdx,requesterIdx));
        if(!isExist) throw new BaseException(BaseResponseStatus.NON_EXIST_POST_PARTICIPATION);
        //2. 존재할 경우 -> accept에 따라 다르게 처리
        if(accept){
            //true -> 해당 request를 받아들일 경우
            //1. 일단 postParticipation을 participate로 변경
            postParticipantRepository.changePostParticipationStatus("PARTICIPATE",postIdx,requesterIdx);
            //2. 그 후 해당 post의 chatroom에 추가 ->
            chatParticipantRepository.save(new ChatParticipant(new ChatNUser(postIdx,requesterIdx),"A",null));
        }
        else{
            //false일 경우 -> requst를 거절
            postParticipantRepository.changePostParticipationStatus("REJECT",postIdx,requesterIdx);
        }
    }
}
