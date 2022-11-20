package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice,Long> {

    @Transactional
    @Modifying
    @Query(value = "update Notice n set n.flag=1 where n.postIdx = :postIdx and n.userIdx = :userIdx")
    void disablePostRelatedNotice(@Param("postIdx") Long postIdx,@Param("userIdx")Long userIdx);

    @Transactional
    @Modifying
    @Query(value = "update Notice n set n.flag = 1 where n.type=:type and n.userIdx = :userIdx")
    void disableFriendRequestNotice(@Param("type") String type,@Param("userIdx") Long userIdx);

    long countByUserIdxAndAndFlag(long userIdx, int flag);

    List<Notice> findAllByUserIdxAndFlag(Long userIdx, int flag);
}
