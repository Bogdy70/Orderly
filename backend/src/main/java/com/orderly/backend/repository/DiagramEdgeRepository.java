package com.orderly.backend.repository;

import com.orderly.backend.entity.DiagramEdgeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagramEdgeRepository extends JpaRepository<DiagramEdgeEntity, Long> {
  List<DiagramEdgeEntity> findByDiagramIdOrderByIdAsc(Long diagramId);
  void deleteByDiagramId(Long diagramId);
}
