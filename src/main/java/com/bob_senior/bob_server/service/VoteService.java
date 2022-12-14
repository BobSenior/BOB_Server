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
        //1. ?????? postIdx??? ?????? activated vote??? ?????? ????????????
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
        // ?????? ????????? ?????? vote ????????? ??????
        boolean already_participated = voteParticipatedRepository.existsVoteParticipatedByUserIdxAndVote_VoteIdx(
                userVoteDTO.getUserIdx(), userVoteDTO.getVoteIdx()
        );
        if(already_participated){
            throw new BaseException(BaseResponseStatus.ALREADY_VOTED);
        }
        //???????????? ????????? -> vote????????? apply??????
        //1.  votecount??? 1 ??????
        vote.setParticipantNum(vote.getParticipantNum()+1);
        voteRepository.save(vote);
        //TODO : ?????? ???????????? ????????? ????????? ??? ?????? or ?????????????????? ??????? select one
        //2. ?????? ????????? ?????? ????????? record??? ??????
        VoteRecord vr = voteRecordRepository.findVoteRecordByVoteId(new VoteId(
                userVoteDTO.getVoteIdx(),
                userVoteDTO.getVoteSelect()
        ));

        vr.setCount(vr.getCount()+1);
        voteRecordRepository.save(vr);

        //3. ????????? vote??? participated??? ??????
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

        //0 . ?????? ???????????? vote?????? ?????? ?????? - votename & timestamp??? ???????????? ???????
        if(voteRepository.existsVoteByTitleAndIsActivated(makeVoteDTO.getTitle(), 1)){
            throw new BaseException(BaseResponseStatus.ALREADY_EXIST_VOTE_CONTENT);
        }

        //0-2 ?????? ?????? post??? activating vote??? ????????? -> ?????? ????????? ongoing ????????? ????????? 1?????? ??????...
        if(voteRepository.existsVoteByPostIdxAndIsActivated(roomIdx,1)){
            throw new BaseException(BaseResponseStatus.ALREADY_EXIST_ONGOING_VOTE);
        }



        if(makeVoteDTO.getVoteType().equals("FIX")){
            makeVoteDTO.setTitle(makeVoteDTO.getLocation()+"$"+makeVoteDTO.getLatitude()+"$"+makeVoteDTO.getLongitude()+"$"+makeVoteDTO.getTime());
        }

        //1. record?????? ?????????
        List<String> list = makeVoteDTO.getContents();
        ArrayList<ShownVoteRecord> records = new ArrayList<>();

        //??????????????? 1?????? ?????? -> generatedValue????????? ??????

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
        //voteIdx??? db??? ?????? ????????????... ?????? ????????? ?????? ??????????????? best->concurrency problem..?

        Vote vote = voteRepository.findVoteByUUID(uuid);
        //db??? ?????? ????????? voteIdx????????????

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

        //?????? ???????????? ???????????? notice?????? -> ????????? ??????
        List<Long> userIdxSet = postParticipantRepository.getAllUserIdxInPostActivated(roomIdx);
        for (Long idx : userIdxSet) {
            if (idx == makeVoteDTO.getMakerIdx()) continue;
            noticeRepository.save(
                    Notice.builder()
                            .userIdx(idx)
                            .postIdx(roomIdx)
                            .flag(0)
                            .content("create")
                            .type("NewVote")
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
        //1. ?????? vote??? maker?????? ??????
        boolean is_owner = voteRepository.existsVoteByVoteIdxAndCreatorIdx(terminateVoteDTO.getVoteIdx(), terminateVoteDTO.getTerminatorIdx());
        if(!is_owner){
            throw new BaseException(BaseResponseStatus.IS_NOT_OWNER_OF_VOTE);
        }
        //2. ????????? ???????????????/?????? ?????? ????????????
        voteRepository.updateStatus(0, terminateVoteDTO.getVoteIdx());
        VoteRecord vr = voteRecordRepository.findTop1ByVoteId_VoteIdxOrderByCountDesc(terminateVoteDTO.getVoteIdx());

        Vote vote = voteRepository.findVoteByVoteIdx(terminateVoteDTO.getVoteIdx());

        //?????? ?????? ????????? ????????????
        List<Long> lists = postParticipantRepository.getAllUserIdxInPostActivated(roomId);
        //appointment ???????????? ??????
        handleVoteResultByType(vote.getVoteType(), vr, vote.getPostIdx(),vote,lists);

    }

    private void handleVoteResultByType(String voteType, VoteRecord vr,Long postIdx,Vote vote,List<Long> list) throws BaseException{
        System.out.println("handlesssss"+voteType);
        //????????? ????????? ?????? ?????? -> problem : ????????? ????????????????
        //TODO : ?????? ????????? ?????? appointment??? ????????? ??????
        //TODO : ?????? table????????? ????????? ??????????????????
        String result = vr.getVoteContent();
        String noticeType = "";
        
        //???????????? total??? ?????????....
        int cur_all = postParticipantRepository.countByPost_PostIdxAndStatus(postIdx,"active").intValue();
        System.out.println("cur_all = " + cur_all);
        
        
        if(!voteType.equals("NORMAL") && vr.getCount()!=cur_all){
            throw new BaseException(BaseResponseStatus.VOTE_RESULT_IS_NOT_UNANIMITY);
        }
        String content = "";
        System.out.println("voteType = " + voteType);
        switch(voteType){
            case "FIX" : {
                //?????? + ????????? ???????????? ???????????? ?????? ?????? ?????? ??????
                //string??? pattern -> location$yyyy/MM/dd HH:mm -> $???????????? split
                //1. ?????????????????? ????????????
                int total_Num = postParticipantRepository.countByPost_PostIdxAndStatus(postIdx,"active").intValue();
                if(vr.getVoteContent().equals("??????")) return;
                System.out.println("total_Num = " + total_Num);
                if(vr.getCount()<total_Num){
                    //reject changing
                    content="????????? ?????????????????????";
                    return;
                }
                String title = vote.getTitle();
                StringTokenizer st = new StringTokenizer(title,"$",false);
                String location = st.nextToken();
                String latitude = st.nextToken();
                String longitude = st.nextToken();
                location = location + "$" + latitude + "$" + longitude;
                String time_string = st.nextToken();
                System.out.println("time_string = " + time_string);
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime localDateTime = LocalDateTime.from(dateTimeFormatter.parse(time_string));
                LocalDateTime now = LocalDateTime.now();
                if(now.isAfter(localDateTime)) throw new BaseException(BaseResponseStatus.DATE_TIME_ERROR);
                Timestamp ts = Timestamp.valueOf(localDateTime);
                postRepository.applyVoteResultDateAndLocation(localDateTime,location,postIdx);
                postRepository.fixPost(postIdx);
                content = "????????? ?????????????????????";
                break;
            }
            case "BREAK" : {
                //?????? ????????? ??????
                //chatParticipant??? ?????? ??????, postParticipant??? ?????? ?????? , Post??????, Chatroom??????
                //post????????? prev = postphoto, postTag, postParticipant,vote, voteRecord, voteParticipated
                //chatroom????????? = chatparticipant, chatmessage?????? ???
                Post post = postRepository.findPostByPostIdx(postIdx);
                //chatRoom?????? prev
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
                content = "????????? ?????????????????????";
                noticeType="CanceledPlan";
                break;
            }
            default:{
                content = "????????? ?????????????????????";
            }
            for (Long idx : list) {
                noticeRepository.save(
                        Notice.builder()
                                .userIdx(idx)
                                .postIdx(postIdx)
                                .type(noticeType)
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
