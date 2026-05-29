package com.orderly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public final class SpaceDtos {
  private SpaceDtos() {
  }

  public record CreateSpaceRequest(
      @NotBlank String name,
      String description,
      String icon,
      String color
  ) {
  }

  public record UpdateSpaceRequest(
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
