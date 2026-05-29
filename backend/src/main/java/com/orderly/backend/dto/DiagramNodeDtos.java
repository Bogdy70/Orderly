package com.orderly.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class DiagramNodeDtos {
  private DiagramNodeDtos() {
  }

  public record CreateDiagramNodeRequest(
      String type,
      @NotBlank String label,
      @NotNull Double x,
      @NotNull Double y,
      @NotNull Double width,
      @NotNull Double height,
      String styleJson,
      String dataJson
  ) {
  }

  public record UpdateDiagramNodeRequest(
      String type,
      String label,
      Double x,
      Double y,
      Double width,
      Double height,
      String styleJson,
      String dataJson
  ) {
  }

  public record DiagramNodeResponse(
      Long id,
      Long diagramId,
      String type,
      String label,
      Double x,
      Double y,
      Double width,
      Double height,
      String styleJson,
      String dataJson,
      Instant createdAt,
      Instant updatedAt
  ) {
  }
}
