package com.orderly.backend.dto;

import java.time.Instant;
import java.util.List;

public final class DiagramDtos {
  private DiagramDtos() {
  }

  public record CreateDiagramRequest(
      Double viewportX,
      Double viewportY,
      Double zoom
  ) {
  }

  public record UpdateDiagramRequest(
      Double viewportX,
      Double viewportY,
      Double zoom
  ) {
  }

  public record DiagramResponse(
      Long id,
      Long blockId,
      Double viewportX,
      Double viewportY,
      Double zoom,
      Instant createdAt,
      Instant updatedAt
  ) {
  }

  public record DiagramFullResponse(
      DiagramResponse diagram,
      List<DiagramNodeDtos.DiagramNodeResponse> nodes,
      List<DiagramEdgeDtos.DiagramEdgeResponse> edges
  ) {
  }
}
