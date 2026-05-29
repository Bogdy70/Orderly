package com.orderly.backend.controller;

import com.orderly.backend.dto.DiagramNodeDtos;
import com.orderly.backend.service.DiagramNodeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DiagramNodeController {
  private final DiagramNodeService diagramNodeService;

  public DiagramNodeController(DiagramNodeService diagramNodeService) {
    this.diagramNodeService = diagramNodeService;
  }

  @PostMapping("/diagrams/{diagramId}/nodes")
  @ResponseStatus(HttpStatus.CREATED)
  public DiagramNodeDtos.DiagramNodeResponse create(
      @PathVariable Long diagramId,
      @Valid @RequestBody DiagramNodeDtos.CreateDiagramNodeRequest request
  ) {
    return diagramNodeService.create(diagramId, request);
  }

  @GetMapping("/diagrams/{diagramId}/nodes")
  public List<DiagramNodeDtos.DiagramNodeResponse> list(@PathVariable Long diagramId) {
    return diagramNodeService.listByDiagram(diagramId);
  }

  @PatchMapping("/diagram-nodes/{nodeId}")
  public DiagramNodeDtos.DiagramNodeResponse update(
      @PathVariable Long nodeId,
      @RequestBody DiagramNodeDtos.UpdateDiagramNodeRequest request
  ) {
    return diagramNodeService.update(nodeId, request);
  }

  @DeleteMapping("/diagram-nodes/{nodeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long nodeId) {
    diagramNodeService.delete(nodeId);
  }
}
