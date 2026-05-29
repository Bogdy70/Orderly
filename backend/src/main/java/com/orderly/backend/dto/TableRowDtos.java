package com.orderly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;

public final class TableRowDtos {
  private TableRowDtos() {
  }

  public record CreateTableRowRequest(
      @NotBlank String title,
      String status,
      String priority,
      LocalDate dueDate,
      Integer position
  ) {
  }

  public record UpdateTableRowRequest(
      String title,
      String status,
      String priority,
      LocalDate dueDate,
      Integer position
  ) {
  }

  public record TableRowResponse(
      Long id,
      Long blockId,
      String title,
      String status,
      String priority,
      LocalDate dueDate,
      Integer position,
      Instant createdAt,
      Instant updatedAt
  ) {
  }
}
