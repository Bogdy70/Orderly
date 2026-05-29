package com.orderly.backend.repository;

import com.orderly.backend.entity.SpaceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<SpaceEntity, Long> {
  List<SpaceEntity> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
