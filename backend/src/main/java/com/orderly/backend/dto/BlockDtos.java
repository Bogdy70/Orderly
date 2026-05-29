package com.orderly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class BlockDtos {
  private BlockDtos() {
  }

  public record CreateBlockRequest(
      @NotBlank String type,
      @NotBlank String title,
      Integer position,
      Double x,
      Double y,
      Double width,
      Double height
  ) {
  }

  public record UpdateBlockRequest(
      String title,
      Integer position,
      Double x,
      Double y,
      Double width,
      Double height
  ) {
  }

  public record BlockResponse(
      Long id,
      Long spaceId,
      String type,
      String title,
      Integer position,
      Double x,
      Double y,
      Double width,
      Double height,
      Instant createdAt,
      Instant updatedAt
  ) {
  }

  public record BlockFullResponse(
      BlockResponse block,
      Object content
  ) {
  }
}
