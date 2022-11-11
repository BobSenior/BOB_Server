package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.vote.entity.VoteParticipated;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteParticipatedRepository extends JpaRepository<VoteParticipated,Integer> {

    boolean existsVoteParticipatedByUserIdxAndVoteIdx(Integer userIdx, Integer voteIdx);

}
