package com.orderly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class ChecklistItemDtos {
  private ChecklistItemDtos() {
  }

  public record CreateChecklistItemRequest(
      @NotBlank String text,
      Boolean done,
      Integer position
  ) {
  }

  public record UpdateChecklistItemRequest(
      String text,
      Boolean done,
      Integer position
  ) {
  }

  public record ChecklistItemResponse(
      Long id,
      Long blockId,
      String text,
      Boolean done,
      Integer position,
      Instant createdAt,
      Instant updatedAt
  ) {
  }
}
