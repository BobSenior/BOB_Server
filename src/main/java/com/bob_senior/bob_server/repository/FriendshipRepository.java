package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.user.entity.FriendId;
import com.bob_senior.bob_server.domain.user.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByFriendInfoAndAndStatus(FriendId id, String status);

    boolean existsByFriendInfo_MaxUserIdxAndFriendInfo_MinUserIdxAndStatus(long maxIdx, long minIdx, String status);

    boolean existsByFriendInfo(FriendId id);

    Friendship getTopByFriendInfoAndStatus(FriendId id, String status);

    @Query(value = "select F from Friendship F where F.status = 'waiting' and (F.friendInfo.maxUserIdx = :userIdx or F.friendInfo.minUserIdx = :userIdx) ")
    Page<Friendship> findAllByUserIdxInWaiting(@Param("userIdx") Long userIdx, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "update Friendship f set f.status = 'active' where f.friendInfo = :id")
    void updateFriendShipACTIVE(FriendId id);

    @Query(value = "select F from Friendship F where F.status = 'active' and F.friendInfo.minUserIdx = :userIdx or F.friendInfo.maxUserIdx = :userIdx")
    Page<Friendship> getFriendList(@Param("userIdx") Long userIdx, Pageable pageable);

}
