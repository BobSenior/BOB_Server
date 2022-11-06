package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.vote.*;
import com.bob_senior.bob_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteParticipatedRepository voteParticipatedRepository;
    private final VoteRecordRepository voteRecordRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final PostRepository postRepository;




    @Autowired
    public VoteService(VoteRepository voteRepository, VoteParticipatedRepository voteParticipatedRepository, VoteRecordRepository voteRecordRepository, ChatRepository chatRepository, ChatParticipantRepository chatParticipantRepository, PostRepository postRepository) {
        this.voteRepository = voteRepository;
        this.voteParticipatedRepository = voteParticipatedRepository;
        this.voteRecordRepository = voteRecordRepository;
        this.chatRepository = chatRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.postRepository = postRepository;
    }




    public boolean checkIfVoteIsValid(int roomIdx, int voteIdx){
        return voteRepository.existsByVoteIdxAndVoteRoomIdx(voteIdx,roomIdx);
    }

    public boolean hasActivatedVoteInRoom(int postIdx){
        return voteRepository.existsVoteByPostIdxAndActivated(postIdx,1);
    }

    public ShownVote getMostRecentVoteInChatroom(int postIdx) throws BaseException{
        Vote vote = voteRepository.findTop1ByPostIdxANDActivated(postIdx,1);
        return ShownVote.builder()
                .voteIdx(vote.getVoteIdx())
                .createdAt(vote.getCreatedAt())
                .title(vote.getTitle())
                .tuples(voteRecordRepository.findAllByVoteId_VoteIdx(vote.getVoteIdx()))
                .total_participated(vote.getParticipatedNum())
                .build();
    }




    @Transactional
    public ShownVote applyUserSelectionToVote(UserVoteDTO userVoteDTO) throws BaseException {
        Vote vote = voteRepository.findVoteByVoteIdx(userVoteDTO.getVoteIdx());
        // 해당 유저가 이미 vote 했는지 확인
        boolean already_participated = voteParticipatedRepository.existsVoteParticipatedByUserIdxAndAndVoteIdx(
                userVoteDTO.getUserIdx(), userVoteDTO.getVoteIdx()
        );
        if(already_participated){
            throw new BaseException(BaseResponseStatus.ALREADY_VOTED);
        }
        //참여하지 않았다 -> vote결과를 apply하기
        //1.  votecount를 1 증가
        vote.setParticipatedNum(vote.getParticipatedNum()+1);
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
                        .voteIdx(userVoteDTO.getVoteIdx())
                        .userIdx(userVoteDTO.getUserIdx())
                        .build()
        );
        //4. 현재까지의 voteResult를 모두에게 spread

        return ShownVote.builder()
                .voteIdx(vote.getVoteIdx())
                .title(vote.getTitle())
                .createdAt(vote.getCreatedAt())
                .tuples(voteRecordRepository.findAllByVoteId_VoteIdx(userVoteDTO.getVoteIdx()))
                .total_participated(vote.getParticipatedNum())
                .build();
    }




    @Transactional
    public ShownVoteDTO makeNewVote(MakeVoteDTO makeVoteDTO, LocalDateTime ldt, Integer roomIdx) throws BaseException{

        //0 . 이미 존재하는 vote인지 한번 검사 - votename & timestamp로 검사하면 될듯?
        if(voteRepository.existsVoteByVoteNameAAndActivated(makeVoteDTO.getTitle(), ldt)){
            throw new BaseException(BaseResponseStatus.ALREADY_EXIST_VOTE_CONTENT);
        }

        //1. record들을 만들기
        List<String> list = makeVoteDTO.getContents();
        ArrayList<VoteRecord> records = new ArrayList<>();

        //항목번호는 1부터 시작

        String uuid = UUID.randomUUID().toString();

        voteRepository.save(Vote.builder()
                .postIdx(roomIdx)
                .title(makeVoteDTO.getTitle())
                .creatorIdx(makeVoteDTO.getMakerIdx())
                .createdAt(ldt)
                .isActivated(true)
                .participatedNum(0)
                .maxNum(Math.toIntExact(chatParticipantRepository.countChatParticipantById_ChatRoomIdx(roomIdx)))
                .voteType("NORMAL").UUID(uuid)
                .build());
        //voteIdx는 db에 의한 자동생성... 일단 생성한 뒤에 가져오는게 best->concurrency problem..?

        Vote vote = voteRepository.findVoteByUUID(uuid);

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
            records.add(record);
        }
        return new ShownVoteDTO(vote,records);
    }

    @Transactional
    public void makeTerminateVote(int roomId, TerminateVoteDTO terminateVoteDTO) throws BaseException {
        //1. 해당 vote의 maker인지 확인
        boolean is_owner = voteRepository.existsVoteByVoteIdxAndCreatorIdx(terminateVoteDTO.getVoteIdx(), terminateVoteDTO.getTerminatorIdx());
        if(!is_owner){
            throw new BaseException(BaseResponseStatus.IS_NOT_OWNER_OF_VOTE);
        }
        //2. 투표를 종료시키기/투표 결과 받아오기
        voteRepository.updateStatus(false, terminateVoteDTO.getVoteIdx());
        VoteRecord vr = voteRecordRepository.findTop1ByCount();

        //3. 투표의 결과를 반영할지.. 일단은 time만 반영한다고 가정해보자.
        //
        Vote vote = voteRepository.findVoteByVoteIdx(terminateVoteDTO.getVoteIdx());
        //appointment 불러오는 과정
        handleVoteResultByType(vote.getVoteType(),vr,vote.getPostIdx());
    }

    private void handleVoteResultByType(String voteType, VoteRecord vr,Integer postIdx) throws BaseException{
        //투표의 결과를 바로 반영 -> problem : 투표가 동률나오면?
        //TODO : 투표 타입에 따라 appointment의 정보를 가공
        String result = vr.getVoteContent();
        switch(voteType){
            case "DATE" :{
                //약속 시간 변경
                Timestamp ts = Timestamp.valueOf(result);
                postRepository.applyVoteResultDate(ts,postIdx);
            }
            case "SPACE":{
                //약속 장소 변경
                postRepository.applyVoteResultLocation(result,postIdx);
            }
            case "CLOSURE":{
                //모집 마감 투표
                boolean closure = false;
                if(result.equals("YES")) closure = true;
                postRepository.applyVoteResultRecruitment(closure,postIdx);
            }
        }
    }
}
