package com.orderly.backend.service;

import com.orderly.backend.dto.BlockDtos;
import com.orderly.backend.dto.ChecklistItemDtos;
import com.orderly.backend.dto.DiagramDtos;
import com.orderly.backend.dto.DiagramEdgeDtos;
import com.orderly.backend.dto.DiagramNodeDtos;
import com.orderly.backend.dto.SpaceDtos;
import com.orderly.backend.dto.TableRowDtos;
import com.orderly.backend.dto.UserDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.ChecklistItemEntity;
import com.orderly.backend.entity.DiagramEdgeEntity;
import com.orderly.backend.entity.DiagramEntity;
import com.orderly.backend.entity.DiagramNodeEntity;
import com.orderly.backend.entity.SpaceEntity;
import com.orderly.backend.entity.TableRowEntity;
import com.orderly.backend.entity.UserEntity;

final class DtoMapper {
  private DtoMapper() {
  }

  static UserDtos.UserResponse toUser(UserEntity user) {
    return new UserDtos.UserResponse(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getKeycloakId(),
        user.getCreatedAt(),
        user.getUpdatedAt()
    );
  }

  static SpaceDtos.SpaceResponse toSpace(SpaceEntity space) {
    return new SpaceDtos.SpaceResponse(
        space.getId(),
        space.getOwner().getId(),
        space.getName(),
        space.getDescription(),
        space.getIcon(),
        space.getColor(),
        space.getCreatedAt(),
        space.getUpdatedAt()
    );
  }

  static BlockDtos.BlockResponse toBlock(BlockEntity block) {
    return new BlockDtos.BlockResponse(
        block.getId(),
        block.getSpace().getId(),
        block.getType().value(),
        block.getTitle(),
        block.getPosition(),
        block.getCreatedAt(),
        block.getUpdatedAt()
    );
  }

  static ChecklistItemDtos.ChecklistItemResponse toChecklistItem(ChecklistItemEntity item) {
    return new ChecklistItemDtos.ChecklistItemResponse(
        item.getId(),
        item.getBlock().getId(),
        item.getText(),
        item.getDone(),
        item.getPosition(),
        item.getCreatedAt(),
        item.getUpdatedAt()
    );
  }

  static TableRowDtos.TableRowResponse toTableRow(TableRowEntity row) {
    return new TableRowDtos.TableRowResponse(
        row.getId(),
        row.getBlock().getId(),
        row.getTitle(),
        row.getStatus(),
        row.getPriority(),
        row.getDueDate(),
        row.getPosition(),
        row.getCreatedAt(),
        row.getUpdatedAt()
    );
  }

  static DiagramDtos.DiagramResponse toDiagram(DiagramEntity diagram) {
    return new DiagramDtos.DiagramResponse(
        diagram.getId(),
        diagram.getBlock().getId(),
        diagram.getViewportX(),
        diagram.getViewportY(),
        diagram.getZoom(),
        diagram.getCreatedAt(),
        diagram.getUpdatedAt()
    );
  }

  static DiagramNodeDtos.DiagramNodeResponse toDiagramNode(DiagramNodeEntity node) {
    return new DiagramNodeDtos.DiagramNodeResponse(
        node.getId(),
        node.getDiagram().getId(),
        node.getType(),
        node.getLabel(),
        node.getX(),
        node.getY(),
        node.getWidth(),
        node.getHeight(),
        node.getStyleJson(),
        node.getDataJson(),
        node.getCreatedAt(),
        node.getUpdatedAt()
    );
  }

  static DiagramEdgeDtos.DiagramEdgeResponse toDiagramEdge(DiagramEdgeEntity edge) {
    return new DiagramEdgeDtos.DiagramEdgeResponse(
        edge.getId(),
        edge.getDiagram().getId(),
        edge.getSourceNode().getId(),
        edge.getTargetNode().getId(),
        edge.getLabel(),
        edge.getType(),
        edge.getStyleJson(),
        edge.getCreatedAt(),
        edge.getUpdatedAt()
    );
  }
}
