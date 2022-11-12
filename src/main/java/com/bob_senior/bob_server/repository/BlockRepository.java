package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.user.entity.Block;
import com.bob_senior.bob_server.domain.user.entity.BlockId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block,Integer> {

    boolean existsById(BlockId id);

}
