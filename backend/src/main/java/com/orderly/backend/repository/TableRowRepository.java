package com.orderly.backend.repository;

import com.orderly.backend.entity.TableRowEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRowRepository extends JpaRepository<TableRowEntity, Long> {
  List<TableRowEntity> findByBlockIdOrderByPositionAscIdAsc(Long blockId);
  void deleteByBlockId(Long blockId);
}
