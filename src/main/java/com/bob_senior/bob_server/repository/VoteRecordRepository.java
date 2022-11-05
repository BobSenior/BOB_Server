package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.vote.VoteId;
import com.bob_senior.bob_server.domain.vote.VoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, VoteId> {

    VoteRecord findVoteRecordByVoteId(VoteId id);

    List<VoteRecord> findAllByVoteId_VoteIdx(Integer voteIdx);

}
