package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface VoteRepository extends JpaRepository<Vote,Integer> {

    Vote findTop1ByCreatedAtAndActivatedTrueAndVoteRoomIdx(Integer roomIdx);

    boolean existsByVoteIdxAndVoteRoomIdx(Integer voteIdx, Integer voteRoomIdx);

    Vote findVoteByVoteIdx(Integer voteIdx);

    Vote findVoteByUUID(String uuid);

    @Query(value = "select v.voteIdx from Vote v where v.UUID = :uuid")
    Integer getVoteIdxByUUID(@Param("uuid") String uuid);

    boolean existsVoteByVoteNameAAndActivated(String voteName, LocalDateTime activated);

}
