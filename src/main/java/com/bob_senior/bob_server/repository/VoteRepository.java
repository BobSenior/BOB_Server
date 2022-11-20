package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.vote.entity.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote,Long> {

    List<Vote> findAllByPostIdxAndIsActivated(long postIdx, String isActivated);

    boolean existsByVoteIdxAndPostIdx(Long voteIdx, Long voteRoomIdx);

    Vote getVoteByPostIdx(long PostIdx);

    Vote findVoteByVoteIdx(Long voteIdx);

    Vote findVoteByUUID(String uuid);

    @Query(value = "select v.voteIdx from Vote v where v.UUID = :uuid")
    Integer getVoteIdxByUUID(@Param("uuid") String uuid);

    boolean existsVoteByTitleAndIsActivated(String voteName, Integer activated);

    boolean existsVoteByVoteIdxAndCreatorIdx(Long voteIdx, Long creatorIdx);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update Vote v set v.isActivated = :state WHERE v.voteIdx = :voteIdx")
    int updateStatus(@Param("state") Integer state, @Param("voteIdx") Long voteIdx);

    boolean existsVoteByPostIdxAndIsActivated(Long postIdx, int activated);


    Vote findVoteByIsActivatedAndPostIdx(Integer activated, Long postIdx);

    Vote findVoteByPostIdxAndIsActivated(long postIdx, int activated);
}
