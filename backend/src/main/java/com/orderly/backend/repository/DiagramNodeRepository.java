package com.orderly.backend.repository;

import com.orderly.backend.entity.DiagramNodeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagramNodeRepository extends JpaRepository<DiagramNodeEntity, Long> {
  List<DiagramNodeEntity> findByDiagramIdOrderByIdAsc(Long diagramId);
  void deleteByDiagramId(Long diagramId);
}
