package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.Post.entity.Post;
import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.notice.entity.Notice;
import com.bob_senior.bob_server.domain.vote.*;
import com.bob_senior.bob_server.domain.vote.entity.Vote;
import com.bob_senior.bob_server.domain.vote.entity.VoteId;
import com.bob_senior.bob_server.domain.vote.entity.VoteParticipated;
import com.bob_senior.bob_server.domain.vote.entity.VoteRecord;
import com.bob_senior.bob_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteParticipatedRepository voteParticipatedRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostParticipantRepository postParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final PostTagRepository postTagRepository;
    private final NoticeRepository noticeRepository;




    @Autowired
    public VoteService(VoteRepository voteRepository, VoteParticipatedRepository voteParticipatedRepository, VoteRecordRepository voteRecordRepository, ChatRepository chatRepository, ChatParticipantRepository chatParticipantRepository, PostRepository postRepository, UserRepository userRepository, PostParticipantRepository postParticipantRepository, ChatMessageRepository chatMessageRepository, PostPhotoRepository postPhotoRepository, PostTagRepository postTagRepository, NoticeRepository noticeRepository) {
        this.voteRepository = voteRepository;
        this.voteParticipatedRepository = voteParticipatedRepository;
        this.voteRecordRepository = voteRecordRepository;
        this.chatRepository = chatRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postParticipantRepository = postParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.postPhotoRepository = postPhotoRepository;
        this.postTagRepository = postTagRepository;
        this.noticeRepository = noticeRepository;
    }




    public boolean checkIfVoteIsValid(Long roomIdx, Long voteIdx){
        return voteRepository.existsByVoteIdxAndPostIdx(voteIdx,roomIdx);
    }

    public boolean hasActivatedVoteInRoom(Long postIdx){
        return voteRepository.existsVoteByPostIdxAndIsActivated(postIdx,1);
    }




    /*public List<ShownVoteHeadDTO> getMostRecentVoteInChatroom(Long postIdx, Long userIdx) throws BaseException{
        //1. 현재 postIdx에 걸린 activated vote를 전부 가져오기
        Vote vote = voteRepository.findVoteByIsActivatedAndPostIdx(1,postIdx);

        List<ShownVoteHeadDTO> dtos = new ArrayList<>();
        for (Vote vote : lists) {
            dtos.add(
                    ShownVoteHeadDTO.builder()
                            .title(vote.getTitle())
                            .voteIdx(vote.getVoteIdx())
                            .participatedNum(vote.getParticipantNum())
                            .participated(
                                    voteParticipatedRepository.existsVoteParticipatedByUserIdxAndVote_VoteIdx(userIdx,vote.getVoteIdx())
                            )
                            .build()
            );
        }
        return dtos;
    }*/


    public ShownVoteDTO getVoteByVoteIdx(Long voteIdx) throws BaseException{
        Vote vote = voteRepository.findVoteByVoteIdx(voteIdx);
        List<VoteRecord> records = voteRecordRepository.findAllByVoteId_VoteIdx(voteIdx);
        List<ShownVoteRecord> dataRecord = new ArrayList<>();
        for (VoteRecord record : records) {
            dataRecord.add(
                    ShownVoteRecord.builder().content(record.getVoteContent()).count(record.getCount()).build()
            );
        }
        return ShownVoteDTO.builder()
                .voteIdx(vote.getVoteIdx())
                .writerIdx(vote.getCreatorIdx())
                .nickname(userRepository.findUserByUserIdx(vote.getCreatorIdx()).getNickName())
                .title(vote.getTitle())
                .records(dataRecord)
                .createdAt(vote.getCreatedAt())
                .totalParticipated(vote.getParticipantNum())
                .build();
    }




    @Transactional
    public void applyUserSelectionToVote(UserVoteDTO userVoteDTO) throws BaseException {
        Vote vote = voteRepository.findVoteByVoteIdx(userVoteDTO.getVoteIdx());
        // 해당 유저가 이미 vote 했는지 확인
        boolean already_participated = voteParticipatedRepository.existsVoteParticipatedByUserIdxAndVote_VoteIdx(
                userVoteDTO.getUserIdx(), userVoteDTO.getVoteIdx()
        );
        if(already_participated){
            throw new BaseException(BaseResponseStatus.ALREADY_VOTED);
        }
        //참여하지 않았다 -> vote결과를 apply하기
        //1.  votecount를 1 증가
        vote.setParticipantNum(vote.getParticipantNum()+1);
        voteRepository.save(vote);
        //TODO : 만약 투표결과 모두가 투표할 시 종료 or 투표개설자가 종료? select one
        //2. 해당 유저의 응답 결과를 record에 반영
        VoteRecord vr = voteRecordRepository.findVoteRecordByVoteId(new VoteId(
                userVoteDTO.getVoteIdx(),
                userVoteDTO.getVoteSelect()
        ));

        vr.setCount(vr.getCount()+1);
        voteRecordRepository.save(vr);

        //3. 유저의 vote를 participated에 보관
        voteParticipatedRepository.save(
                VoteParticipated.builder()
                        .vote(vote)
                        .userIdx(userVoteDTO.getUserIdx())
                        .voteRecordIdx(vr.getVoteRecordIdx())
                        .build()
        );
    }




    @Transactional
    public ShownVoteDTO makeNewVote(MakeVoteDTO makeVoteDTO, LocalDateTime ldt, Long roomIdx) throws BaseException{

        //0 . 이미 존재하는 vote인지 한번 검사 - votename & timestamp로 검사하면 될듯?
        if(voteRepository.existsVoteByTitleAndIsActivated(makeVoteDTO.getTitle(), 1)){
            throw new BaseException(BaseResponseStatus.ALREADY_EXIST_VOTE_CONTENT);
        }

        //0-2 이미 해당 post에 activating vote가 존재시 -> 정책 자체가 ongoing 투표는 무조건 1개로 고정...
        if(voteRepository.existsVoteByPostIdxAndIsActivated(roomIdx,1)){
            throw new BaseException(BaseResponseStatus.ALREADY_EXIST_ONGOING_VOTE);
        }

        if(makeVoteDTO.getVoteType().equals("FIX")){
            makeVoteDTO.setTitle(makeVoteDTO.getLocation()+"$"+makeVoteDTO.getLatitude()+"$"+makeVoteDTO.getLongitude()+"$"+makeVoteDTO.getTime());
        }

        //1. record들을 만들기
        List<String> list = makeVoteDTO.getContents();
        ArrayList<ShownVoteRecord> records = new ArrayList<>();

        //항목번호는 1부터 시작 -> generatedValue그대로 사용

        String uuid = UUID.randomUUID().toString();

        voteRepository.save(Vote.builder()
                .postIdx(roomIdx)
                .title(makeVoteDTO.getTitle())
                .creatorIdx(makeVoteDTO.getMakerIdx())
                .createdAt(ldt)
                .isActivated(1)
                .participantNum(0)
                .voteType(makeVoteDTO.getVoteType())
                .UUID(uuid)
                .build());
        //voteIdx는 db에 의한 자동생성... 일단 생성한 뒤에 가져오는게 best->concurrency problem..?

        Vote vote = voteRepository.findVoteByUUID(uuid);
        //db에 의해 생성된 voteIdx가져오기

        int select_count = 1;
        for (String s : list) {
            VoteId voteId = new VoteId(vote.getVoteIdx(), select_count);
            select_count+=1;
            VoteRecord record = VoteRecord.builder()
                    .voteId(voteId)
                    .voteContent(s)
                    .count(0)
                    .build();
            voteRecordRepository.save(record);
            records.add(
                    ShownVoteRecord.builder().content(s).count(0).build()
            );
        }

        //해당 게시글의 모두에게 notice생성 -> 게시자 말고
        List<Long> userIdxSet = postParticipantRepository.getAllUserIdxInPostActivated(roomIdx);
        for (Long idx : userIdxSet) {
            if (idx == makeVoteDTO.getMakerIdx()) continue;
            noticeRepository.save(
                    Notice.builder()
                            .userIdx(idx)
                            .postIdx(roomIdx)
                            .flag(0)
                            .content("투표가 생성되었습니다.")
                            .type("makeVote")
                            .build()
            );
        }
        return ShownVoteDTO.builder()
                .voteIdx(vote.getVoteIdx())
                .writerIdx(vote.getCreatorIdx())
                .nickname(userRepository.findUserByUserIdx(vote.getCreatorIdx()).getNickName())
                .createdAt(vote.getCreatedAt())
                .title(vote.getTitle())
                .totalParticipated(vote.getParticipantNum())
                .records(records)
                .build();
    }

    @Transactional
    public void makeTerminateVote(Long roomId, TerminateVoteDTO terminateVoteDTO) throws BaseException {
        //1. 해당 vote의 maker인지 확인
        boolean is_owner = voteRepository.existsVoteByVoteIdxAndCreatorIdx(terminateVoteDTO.getVoteIdx(), terminateVoteDTO.getTerminatorIdx());
        if(!is_owner){
            throw new BaseException(BaseResponseStatus.IS_NOT_OWNER_OF_VOTE);
        }
        //2. 투표를 종료시키기/투표 결과 받아오기
        voteRepository.updateStatus(0, terminateVoteDTO.getVoteIdx());
        VoteRecord vr = voteRecordRepository.findTop1ByVoteId_VoteIdxOrderByCountDesc(terminateVoteDTO.getVoteIdx());

        Vote vote = voteRepository.findVoteByVoteIdx(terminateVoteDTO.getVoteIdx());

        //모든 멤버 리스트 받아오기
        List<Long> lists = postParticipantRepository.getAllUserIdxInPostActivated(roomId);
        //appointment 불러오는 과정
        handleVoteResultByType(vote.getVoteType(), vr, vote.getPostIdx(),vote,lists);

    }

    private void handleVoteResultByType(String voteType, VoteRecord vr,Long postIdx,Vote vote,List<Long> list) throws BaseException{
        //투표의 결과를 바로 반영 -> problem : 투표가 동률나오면?
        //TODO : 투표 타입에 따라 appointment의 정보를 가공
        //TODO : 알람 table사용시 여기에 저장해야되나
        String result = vr.getVoteContent();
        String noticeType = "";
        if(!voteType.equals("NORMAL") && vr.getCount()!=postRepository.getMaximumParticipationNumFromPost(vote.getPostIdx())){
            throw new BaseException(BaseResponseStatus.VOTE_RESULT_IS_NOT_UNANIMITY);
        }
        String content = "";
        switch(voteType){
            case "FIX" : {
                //시간 + 장소의 데이터로 넘어오게 되면 이를 바로 반영
                //string의 pattern -> location$yyyy/MM/dd HH:mm -> $기준으로 split
                //1. 만장일치인지 확인하기
                int total_Num = postParticipantRepository.countByPost_PostIdxAndStatus(postIdx,"active").intValue();
                if(vr.getCount()<total_Num){
                    //reject changing
                    content="투표가 종료되었습니다";
                    noticeType="VoteEnd";
                    break;
                }
                String title = vote.getTitle();
                StringTokenizer st = new StringTokenizer(title,"$",false);
                String location = st.nextToken();
                String latitude = st.nextToken();
                String longitude = st.nextToken();
                location = location + "$" + latitude + "$" + longitude;
                String time_string = st.nextToken();
                System.out.println("time_string = " + time_string);
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime.from(dateTimeFormatter.parse(time_string));
                LocalDateTime now = LocalDateTime.now();
                if(now.isAfter(localDateTime)) throw new BaseException(BaseResponseStatus.DATE_TIME_ERROR);
                Timestamp ts = Timestamp.valueOf(localDateTime);
                postRepository.applyVoteResultDateAndLocation(localDateTime,location,postIdx);
                postRepository.fixPost(postIdx);
                content = "약속이 확정되었습니다";
                break;
            }
            case "BREAK" : {
                //해당 게시글 폭파
                //chatParticipant를 모두 제거, postParticipant도 모두 제거 , Post제거, Chatroom제거
                //post삭제시 prev = postphoto, postTag, postParticipant,vote, voteRecord, voteParticipated
                //chatroom삭제시 = chatparticipant, chatmessage삭제 요
                Post post = postRepository.findPostByPostIdx(postIdx);
                //chatRoom착제 prev
                chatParticipantRepository.deleteAllParticipationInChatroom(post.getChatRoomIdx());
                chatMessageRepository.deleteAllByChatRoom_ChatRoomIdx(post.getChatRoomIdx());
                chatRepository.deleteById(post.getChatRoomIdx());
                postPhotoRepository.deleteAllByPost(post);
                postTagRepository.deleteAllByPost(post);
                voteParticipatedRepository.deleteAllByVote(voteRepository.findVoteByVoteIdx(vr.getVoteId().getVoteIdx()));
                voteRecordRepository.deleteAllByVoteId_VoteIdx(vr.getVoteId().getVoteIdx());
                voteRepository.deleteById(vr.getVoteId().getVoteIdx());
                postParticipantRepository.deleteAllParticipantInPost(postIdx);
                postRepository.delete(post);
                content = "약속이 파기되었습니다";
                noticeType="CanceledPlan";
                break;
            }
            default:{
                content = "투표가 종료되었습니다";
            }
            for (Long idx : list) {
                noticeRepository.save(
                        Notice.builder()
                                .userIdx(idx)
                                .postIdx(postIdx)
                                .type(voteType)
                                .content(content)
                                .flag(0)
                                .build()
                );
            }
        }
    }

    public List<ShownVoteHeadDTO> getMostRecentVoteInChatroom(Long roomIdx, long userIdx) throws BaseException{
        List<Vote> list = voteRepository.findAllByPostIdxAndIsActivated(roomIdx,"finished");
        List<ShownVoteHeadDTO> data = new ArrayList<>();
        for (Vote vote : list) {
            Long num = voteParticipatedRepository.countByVote_VoteIdx(vote.getVoteIdx());
            data.add(
              ShownVoteHeadDTO.builder()
                      .title(vote.getTitle())
                      .voteIdx(vote.getVoteIdx())
                      .participatedNum(num.intValue())
                      .build()
            );
        }
        return data;
    }
}
