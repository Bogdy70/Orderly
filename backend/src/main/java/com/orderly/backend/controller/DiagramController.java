package com.orderly.backend.controller;

import com.orderly.backend.dto.DiagramDtos;
import com.orderly.backend.service.DiagramService;
import jakarta.validation.Valid;
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
public class DiagramController {
  private final DiagramService diagramService;

  public DiagramController(DiagramService diagramService) {
    this.diagramService = diagramService;
  }

  @PostMapping("/blocks/{blockId}/diagram")
  @ResponseStatus(HttpStatus.CREATED)
  public DiagramDtos.DiagramResponse create(
      @PathVariable Long blockId,
      @Valid @RequestBody DiagramDtos.CreateDiagramRequest request
  ) {
    return diagramService.create(blockId, request);
  }

  @GetMapping("/blocks/{blockId}/diagram")
  public DiagramDtos.DiagramResponse getByBlock(@PathVariable Long blockId) {
    return diagramService.getByBlock(blockId);
  }

  @PatchMapping("/diagrams/{diagramId}")
  public DiagramDtos.DiagramResponse update(
      @PathVariable Long diagramId,
      @RequestBody DiagramDtos.UpdateDiagramRequest request
  ) {
    return diagramService.update(diagramId, request);
  }

  @DeleteMapping("/diagrams/{diagramId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long diagramId) {
    diagramService.delete(diagramId);
  }
}
