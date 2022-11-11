package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.vote.entity.VoteId;
import com.bob_senior.bob_server.domain.vote.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, VoteId> {

    VoteRecord findVoteRecordByVoteId(VoteId id);

    List<VoteRecord> findAllByVoteId_VoteIdx(Integer voteIdx);

    VoteRecord findFirstByVoteId_VoteIdxOrderByCountDesc(Integer voteIdx);

}
