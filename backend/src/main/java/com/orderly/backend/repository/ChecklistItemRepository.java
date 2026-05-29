package com.orderly.backend.repository;

import com.orderly.backend.entity.ChecklistItemEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItemEntity, Long> {
  List<ChecklistItemEntity> findByBlockIdOrderByPositionAscIdAsc(Long blockId);
  void deleteByBlockId(Long blockId);
}
