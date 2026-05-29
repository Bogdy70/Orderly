package com.orderly.backend.repository;

import com.orderly.backend.entity.DiagramEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagramRepository extends JpaRepository<DiagramEntity, Long> {
  Optional<DiagramEntity> findByBlockId(Long blockId);
  void deleteByBlockId(Long blockId);
}
