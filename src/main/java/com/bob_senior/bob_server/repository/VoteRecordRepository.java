package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.vote.entity.VoteId;
import com.bob_senior.bob_server.domain.vote.entity.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, Long> {

    VoteRecord findVoteRecordByVoteId(VoteId id);

    List<VoteRecord> findAllByVoteId_VoteIdx(Long voteIdx);

    VoteRecord findTop1ByVoteId_VoteIdxOrderByCountDesc(Long voteIdx);

    @Transactional
    void deleteAllByVoteId_VoteIdx(long voteIdx);

}
