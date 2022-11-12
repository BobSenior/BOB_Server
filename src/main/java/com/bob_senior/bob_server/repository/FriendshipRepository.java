package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.user.entity.FriendId;
import com.bob_senior.bob_server.domain.user.entity.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {

    boolean existsByIdAndAndStatus(FriendId id, String status);

    @Query(value = "select F from Friendship F where F.status = 'WAITING' and (F.id.maxUserIdx = :userIdx or F.id.minUserIdx = :userIdx) ")
    Page<Friendship> findAllByUserIdxInWaiting(@Param("userIdx") Integer userIdx, Pageable pageable);

    @Modifying
    @Query(value = "update Friendship f set f.status = 'ACTIVE' where f.id = :id")
    void updateFriendShipACTIVE(FriendId id);


}
