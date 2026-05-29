package com.orderly.backend.repository;

import com.orderly.backend.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByKeycloakId(String keycloakId);
  Optional<UserEntity> findByEmail(String email);
  boolean existsByKeycloakId(String keycloakId);
  boolean existsByEmail(String email);
  boolean existsByUsername(String username);
}
