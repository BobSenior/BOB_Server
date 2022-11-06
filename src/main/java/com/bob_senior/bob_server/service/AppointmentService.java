package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.Post.Post;
import com.bob_senior.bob_server.domain.Post.PostParticipant;
import com.bob_senior.bob_server.domain.appointment.AppointmentViewDTO;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.user.User;
import com.bob_senior.bob_server.domain.user.UserProfile;
import com.bob_senior.bob_server.repository.PostParticipantRepository;
import com.bob_senior.bob_server.repository.PostRepository;
import com.bob_senior.bob_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {

    private final PostRepository postRepository;
    private final PostParticipantRepository postParticipantRepository;
    private final UserRepository userRepository;

    @Autowired
    public AppointmentService(PostRepository postRepository, PostParticipantRepository postParticipantRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.postParticipantRepository = postParticipantRepository;
        this.userRepository = userRepository;
    }

    public AppointmentViewDTO getAppointmentData(Integer postIdx) throws BaseException {
        Post post = postRepository.findPostByPostIdx(postIdx);

        List<UserProfile> buyer = new ArrayList<>();
        List<UserProfile> receiver = new ArrayList<>();

        List<PostParticipant> buyer_prev = postParticipantRepository.findPostParticipantsByPostAndUser_PostIdxAndStatus(postIdx,"BUYER");
        List<PostParticipant> receiver_prev = postParticipantRepository.findPostParticipantsByPostAndUser_PostIdxAndStatus(postIdx,"RECEIVER");
        for (PostParticipant participant : buyer_prev) {
            User user = userRepository.findUserByUserIdx(participant.getPostAndUser().getUserIdx());
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
            User user = userRepository.findUserByUserIdx(participant.getPostAndUser().getUserIdx());
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
}
