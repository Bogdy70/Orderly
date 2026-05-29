package com.orderly.backend.service;

import com.orderly.backend.dto.BlockDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.entity.ChecklistItemEntity;
import com.orderly.backend.entity.DiagramEntity;
import com.orderly.backend.entity.DiagramNodeEntity;
import com.orderly.backend.entity.TableRowEntity;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlockConversionService {
  private final BlockService blockService;
  private final BlockFullService blockFullService;
  private final ChecklistItemService checklistItemService;
  private final TableRowService tableRowService;
  private final DiagramService diagramService;
  private final DiagramNodeService diagramNodeService;

  public BlockConversionService(
      BlockService blockService,
      BlockFullService blockFullService,
      ChecklistItemService checklistItemService,
      TableRowService tableRowService,
      DiagramService diagramService,
      DiagramNodeService diagramNodeService
  ) {
    this.blockService = blockService;
    this.blockFullService = blockFullService;
    this.checklistItemService = checklistItemService;
    this.tableRowService = tableRowService;
    this.diagramService = diagramService;
    this.diagramNodeService = diagramNodeService;
  }

  public BlockDtos.BlockFullResponse convert(Long blockId, BlockType targetType) {
    BlockEntity source = blockService.getEntity(blockId);
    BlockEntity target = blockService.createEntity(
        source.getSpace().getId(),
        targetType,
        source.getTitle() + " - " + titleSuffix(targetType),
        source.getPosition() + 1
    );

    switch (targetType) {
      case CHECKLIST -> convertToChecklist(source, target);
      case TABLE -> convertToTable(source, target);
      case DIAGRAM -> convertToDiagram(source, target);
    }

    return blockFullService.getFull(target.getId());
  }

  private void convertToChecklist(BlockEntity source, BlockEntity target) {
    switch (source.getType()) {
      case CHECKLIST -> checklistItemService.entitiesByBlock(source.getId())
          .forEach(item -> checklistItemService.createEntity(target, item.getText(), item.getDone(), item.getPosition()));
      case TABLE -> tableRowService.entitiesByBlock(source.getId())
          .forEach(row -> checklistItemService.createEntity(target, row.getTitle(), "done".equals(row.getStatus()), row.getPosition()));
      case DIAGRAM -> diagramService.findByBlock(source.getId())
          .ifPresent(diagram -> diagramNodeService.entitiesByDiagram(diagram.getId())
              .forEach(node -> checklistItemService.createEntity(target, node.getLabel(), false, node.getId().intValue())));
    }
  }

  private void convertToTable(BlockEntity source, BlockEntity target) {
    switch (source.getType()) {
      case CHECKLIST -> checklistItemService.entitiesByBlock(source.getId())
          .forEach(item -> tableRowService.createEntity(
              target,
              item.getText(),
              item.getDone() ? "done" : "todo",
              null,
              null,
              item.getPosition()
          ));
      case TABLE -> tableRowService.entitiesByBlock(source.getId())
          .forEach(row -> tableRowService.createEntity(
              target,
              row.getTitle(),
              row.getStatus(),
              row.getPriority(),
              row.getDueDate(),
              row.getPosition()
          ));
      case DIAGRAM -> diagramService.findByBlock(source.getId())
          .ifPresent(diagram -> diagramNodeService.entitiesByDiagram(diagram.getId())
              .forEach(node -> tableRowService.createEntity(target, node.getLabel(), "todo", null, null, node.getId().intValue())));
    }
  }

  private void convertToDiagram(BlockEntity source, BlockEntity target) {
    DiagramEntity diagram = diagramService.createEntity(target, 0.0, 0.0, 1.0);

    switch (source.getType()) {
      case CHECKLIST -> {
        List<ChecklistItemEntity> items = checklistItemService.entitiesByBlock(source.getId());
        for (int index = 0; index < items.size(); index++) {
          ChecklistItemEntity item = items.get(index);
          diagramNodeService.createEntity(
              diagram,
              "checklist-item",
              item.getText(),
              80.0,
              80.0 + index * 120.0,
              220.0,
              72.0,
              item.getDone() ? "{\"checked\":true}" : "{\"checked\":false}",
              "{\"source\":\"checklist\"}"
          );
        }
      }
      case TABLE -> {
        List<TableRowEntity> rows = tableRowService.entitiesByBlock(source.getId());
        for (int index = 0; index < rows.size(); index++) {
          TableRowEntity row = rows.get(index);
          diagramNodeService.createEntity(
              diagram,
              "table-row",
              row.getTitle(),
              80.0,
              80.0 + index * 120.0,
              240.0,
              78.0,
              "{}",
              tableRowDataJson(row)
          );
        }
      }
      case DIAGRAM -> diagramService.findByBlock(source.getId()).ifPresent(sourceDiagram -> {
        List<DiagramNodeEntity> nodes = diagramNodeService.entitiesByDiagram(sourceDiagram.getId());
        for (DiagramNodeEntity node : nodes) {
          diagramNodeService.createEntity(
              diagram,
              node.getType(),
              node.getLabel(),
              node.getX(),
              node.getY(),
              node.getWidth(),
              node.getHeight(),
              node.getStyleJson(),
              node.getDataJson()
          );
        }
      });
    }
  }

  private String tableRowDataJson(TableRowEntity row) {
    String dueDate = row.getDueDate() == null ? null : row.getDueDate().format(DateTimeFormatter.ISO_DATE);
    return "{"
        + "\"source\":\"table\","
        + "\"status\":\"" + escapeJson(row.getStatus()) + "\","
        + "\"priority\":" + jsonString(row.getPriority()) + ","
        + "\"dueDate\":" + jsonString(dueDate)
        + "}";
  }

  private String jsonString(String value) {
    return value == null ? "null" : "\"" + escapeJson(value) + "\"";
  }

  private String escapeJson(String value) {
    return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String titleSuffix(BlockType targetType) {
    return switch (targetType) {
      case CHECKLIST -> "Checklist";
      case TABLE -> "Table";
      case DIAGRAM -> "Diagram";
    };
  }
}
