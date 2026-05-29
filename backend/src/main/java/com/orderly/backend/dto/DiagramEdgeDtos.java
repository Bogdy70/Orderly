package com.orderly.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class DiagramEdgeDtos {
  private DiagramEdgeDtos() {
  }

  public record CreateDiagramEdgeRequest(
      @NotNull Long sourceNodeId,
      @NotNull Long targetNodeId,
      String label,
      String type,
      String styleJson
  ) {
  }

  public record UpdateDiagramEdgeRequest(
      Long sourceNodeId,
      Long targetNodeId,
      String label,
      String type,
      String styleJson
  ) {
  }

  public record DiagramEdgeResponse(
      Long id,
      Long diagramId,
      Long sourceNodeId,
      Long targetNodeId,
      String label,
      String type,
      String styleJson,
      Instant createdAt,
      Instant updatedAt
  ) {
  }
}
