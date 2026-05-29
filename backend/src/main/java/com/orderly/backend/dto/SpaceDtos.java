package com.orderly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class SpaceDtos {
  private SpaceDtos() {
  }

  public record CreateSpaceRequest(
      @NotNull Long ownerId,
      @NotBlank String name,
      String description,
      String icon,
      String color
  ) {
  }

  public record UpdateSpaceRequest(
      Long ownerId,
      String name,
      String description,
      String icon,
      String color
  ) {
  }

  public record SpaceResponse(
      Long id,
      Long ownerId,
      String name,
      String description,
      String icon,
      String color,
      Instant createdAt,
      Instant updatedAt
  ) {
  }

  public record SpaceFullResponse(
      SpaceResponse space,
      List<BlockDtos.BlockFullResponse> blocks
  ) {
  }
}
