package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.vote.*;
import com.bob_senior.bob_server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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




    @Autowired
    public VoteService(VoteRepository voteRepository, VoteParticipatedRepository voteParticipatedRepository, VoteRecordRepository voteRecordRepository, ChatRepository chatRepository, ChatParticipantRepository chatParticipantRepository) {
        this.voteRepository = voteRepository;
        this.voteParticipatedRepository = voteParticipatedRepository;
        this.voteRecordRepository = voteRecordRepository;
        this.chatRepository = chatRepository;
        this.chatParticipantRepository = chatParticipantRepository;
    }




    public boolean checkIfVoteIsValid(int roomIdx, int voteIdx){
        return voteRepository.existsByVoteIdxAndVoteRoomIdx(voteIdx,roomIdx);
    }




    public VoteResult applyUserSelectionToVote(UserVoteDTO userVoteDTO) throws BaseException {
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

        return VoteResult.builder()
                .voteIdx(vote.getVoteIdx())
                .title(vote.getVoteName())
                .createdAt(vote.getCreatedAt())
                .tuples(voteRecordRepository.findAllByVoteId_VoteIdx(userVoteDTO.getVoteIdx()))
                .total_participated(vote.getParticipatedNum())
                .build();
    }




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
                .voteRoomIdx(roomIdx)
                .voteName(makeVoteDTO.getTitle())
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
}
