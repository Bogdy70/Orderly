package com.orderly.backend.service;

import com.orderly.backend.dto.DiagramDtos;
import com.orderly.backend.entity.BlockEntity;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.entity.DiagramEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.DiagramRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiagramService {
  private final DiagramRepository diagramRepository;
  private final BlockService blockService;
  private final ValidationService validationService;

  public DiagramService(DiagramRepository diagramRepository, BlockService blockService, ValidationService validationService) {
    this.diagramRepository = diagramRepository;
    this.blockService = blockService;
    this.validationService = validationService;
  }

  public DiagramDtos.DiagramResponse create(Long blockId, DiagramDtos.CreateDiagramRequest request) {
    BlockEntity block = blockService.getEntity(blockId);
    validationService.requireBlockType(block, BlockType.DIAGRAM, "Diagrams");
    if (diagramRepository.findByBlockId(blockId).isPresent()) {
      throw ApiException.badRequest("This diagram block already has a diagram.");
    }
    return DtoMapper.toDiagram(createEntity(block, request.viewportX(), request.viewportY(), request.zoom()));
  }

  DiagramEntity createEntity(BlockEntity block, Double viewportX, Double viewportY, Double zoom) {
    DiagramEntity diagram = new DiagramEntity();
    diagram.setBlock(block);
    diagram.setViewportX(viewportX == null ? 0.0 : viewportX);
    diagram.setViewportY(viewportY == null ? 0.0 : viewportY);
    diagram.setZoom(zoom == null ? 1.0 : zoom);
    return diagramRepository.save(diagram);
  }

  @Transactional(readOnly = true)
  public DiagramDtos.DiagramResponse getByBlock(Long blockId) {
    BlockEntity block = blockService.getEntity(blockId);
    validationService.requireBlockType(block, BlockType.DIAGRAM, "Diagrams");
    return DtoMapper.toDiagram(getEntityByBlock(blockId));
  }

  public DiagramDtos.DiagramResponse update(Long id, DiagramDtos.UpdateDiagramRequest request) {
    DiagramEntity diagram = getEntity(id);
    if (request.viewportX() != null) diagram.setViewportX(request.viewportX());
    if (request.viewportY() != null) diagram.setViewportY(request.viewportY());
    if (request.zoom() != null) diagram.setZoom(request.zoom());
    return DtoMapper.toDiagram(diagram);
  }

  public void delete(Long id) {
    diagramRepository.delete(getEntity(id));
  }

  DiagramEntity getEntity(Long id) {
    return diagramRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Diagram not found."));
  }

  Optional<DiagramEntity> findByBlock(Long blockId) {
    return diagramRepository.findByBlockId(blockId);
  }

  DiagramEntity getEntityByBlock(Long blockId) {
    return diagramRepository.findByBlockId(blockId)
        .orElseThrow(() -> ApiException.notFound("Diagram not found for block."));
  }
}
