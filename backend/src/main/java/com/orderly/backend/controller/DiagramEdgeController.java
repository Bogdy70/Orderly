package com.orderly.backend.controller;

import com.orderly.backend.dto.DiagramEdgeDtos;
import com.orderly.backend.service.DiagramEdgeService;
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
public class DiagramEdgeController {
  private final DiagramEdgeService diagramEdgeService;

  public DiagramEdgeController(DiagramEdgeService diagramEdgeService) {
    this.diagramEdgeService = diagramEdgeService;
  }

  @PostMapping("/diagrams/{diagramId}/edges")
  @ResponseStatus(HttpStatus.CREATED)
  public DiagramEdgeDtos.DiagramEdgeResponse create(
      @PathVariable Long diagramId,
      @Valid @RequestBody DiagramEdgeDtos.CreateDiagramEdgeRequest request
  ) {
    return diagramEdgeService.create(diagramId, request);
  }

  @GetMapping("/diagrams/{diagramId}/edges")
  public List<DiagramEdgeDtos.DiagramEdgeResponse> list(@PathVariable Long diagramId) {
    return diagramEdgeService.listByDiagram(diagramId);
  }

  @PatchMapping("/diagram-edges/{edgeId}")
  public DiagramEdgeDtos.DiagramEdgeResponse update(
      @PathVariable Long edgeId,
      @RequestBody DiagramEdgeDtos.UpdateDiagramEdgeRequest request
  ) {
    return diagramEdgeService.update(edgeId, request);
  }

  @DeleteMapping("/diagram-edges/{edgeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long edgeId) {
    diagramEdgeService.delete(edgeId);
  }
}
