package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote,Integer> {

    Vote findTop1ByCreatedAtAndActivatedTrueAndVoteRoomIdx(Integer roomIdx);

    boolean existsByVoteIdxAndVoteRoomIdx(Integer voteIdx, Integer voteRoomIdx);

    Vote findVoteByVoteIdx(Integer voteIdx);

}
