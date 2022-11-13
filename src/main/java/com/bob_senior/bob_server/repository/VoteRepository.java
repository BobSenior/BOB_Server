package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.vote.entity.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface VoteRepository extends JpaRepository<Vote,Integer> {

    boolean existsByVoteIdxAndPostIdx(Integer voteIdx, Integer voteRoomIdx);


    Vote findVoteByVoteIdx(Integer voteIdx);

    Vote findVoteByUUID(String uuid);

    @Query(value = "select v.voteIdx from Vote v where v.UUID = :uuid")
    Integer getVoteIdxByUUID(@Param("uuid") String uuid);

    boolean existsVoteByTitleAndIsActivated(String voteName, LocalDateTime activated);

    boolean existsVoteByVoteIdxAndCreatorIdx(Integer voteIdx, Integer creatorIdx);

    @Modifying(clearAutomatically = true)
    @Query(value = "update Vote v set v.isActivated = :state WHERE v.voteIdx = :voteIdx")
    int updateStatus(@Param("state") boolean state, @Param("voteIdx") Integer voteIdx);

    boolean existsVoteByPostIdxAndIsActivated(Integer postIdx, int activated);


    Page<Vote> findAllByIsActivatedAndPostIdx(String activated, Integer postIdx, Pageable pageable);

}
