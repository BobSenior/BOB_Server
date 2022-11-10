package com.bob_senior.bob_server.repository;

import com.bob_senior.bob_server.domain.user.Block;
import com.bob_senior.bob_server.domain.user.BlockId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block,BlockId> {

    boolean existsById(BlockId id);

}
