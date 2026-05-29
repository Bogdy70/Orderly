package com.orderly.backend.repository;

import com.orderly.backend.entity.BlockEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<BlockEntity, Long> {
  List<BlockEntity> findBySpaceIdOrderByPositionAscIdAsc(Long spaceId);
}
