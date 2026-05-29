package com.orderly.backend.service;

import com.orderly.backend.dto.DiagramDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.DiagramEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BlockContentService {
  private final ChecklistItemService checklistItemService;
  private final TableRowService tableRowService;
  private final DiagramService diagramService;
  private final DiagramNodeService diagramNodeService;
  private final DiagramEdgeService diagramEdgeService;

  public BlockContentService(
      ChecklistItemService checklistItemService,
      TableRowService tableRowService,
      DiagramService diagramService,
      DiagramNodeService diagramNodeService,
      DiagramEdgeService diagramEdgeService
  ) {
    this.checklistItemService = checklistItemService;
    this.tableRowService = tableRowService;
    this.diagramService = diagramService;
    this.diagramNodeService = diagramNodeService;
    this.diagramEdgeService = diagramEdgeService;
  }

  public Object contentFor(BlockEntity block) {
    return switch (block.getType()) {
      case CHECKLIST -> checklistItemService.entitiesByBlock(block.getId())
          .stream()
          .map(DtoMapper::toChecklistItem)
          .toList();
      case TABLE -> tableRowService.entitiesByBlock(block.getId())
          .stream()
          .map(DtoMapper::toTableRow)
          .toList();
      case DIAGRAM -> diagramService.findByBlock(block.getId())
          .map(this::diagramFull)
          .orElse(null);
    };
  }

  DiagramDtos.DiagramFullResponse diagramFull(DiagramEntity diagram) {
    return new DiagramDtos.DiagramFullResponse(
        DtoMapper.toDiagram(diagram),
        diagramNodeService.entitiesByDiagram(diagram.getId()).stream().map(DtoMapper::toDiagramNode).toList(),
        diagramEdgeService.entitiesByDiagram(diagram.getId()).stream().map(DtoMapper::toDiagramEdge).toList()
    );
  }
}
