package com.bob_senior.bob_server.service;

import com.bob_senior.bob_server.domain.base.BaseException;
import com.bob_senior.bob_server.domain.base.BaseResponseStatus;
import com.bob_senior.bob_server.domain.vote.*;
import com.bob_senior.bob_server.repository.VoteParticipatedRepository;
import com.bob_senior.bob_server.repository.VoteRecordRepository;
import com.bob_senior.bob_server.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteParticipatedRepository voteParticipatedRepository;
    private final VoteRecordRepository voteRecordRepository;

    @Autowired
    public VoteService(VoteRepository voteRepository, VoteParticipatedRepository voteParticipatedRepository, VoteRecordRepository voteRecordRepository) {
        this.voteRepository = voteRepository;
        this.voteParticipatedRepository = voteParticipatedRepository;
        this.voteRecordRepository = voteRecordRepository;
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

}
