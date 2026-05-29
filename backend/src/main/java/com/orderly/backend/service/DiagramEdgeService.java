package com.orderly.backend.service;

import com.orderly.backend.dto.DiagramEdgeDtos;
import com.orderly.backend.entity.DiagramEdgeEntity;
import com.orderly.backend.entity.DiagramEntity;
import com.orderly.backend.entity.DiagramNodeEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.DiagramEdgeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiagramEdgeService {
  private final DiagramEdgeRepository diagramEdgeRepository;
  private final DiagramService diagramService;
  private final DiagramNodeService diagramNodeService;

  public DiagramEdgeService(
      DiagramEdgeRepository diagramEdgeRepository,
      DiagramService diagramService,
      DiagramNodeService diagramNodeService
  ) {
    this.diagramEdgeRepository = diagramEdgeRepository;
    this.diagramService = diagramService;
    this.diagramNodeService = diagramNodeService;
  }

  public DiagramEdgeDtos.DiagramEdgeResponse create(Long diagramId, DiagramEdgeDtos.CreateDiagramEdgeRequest request) {
    DiagramEntity diagram = diagramService.getEntity(diagramId);
    DiagramNodeEntity source = diagramNodeService.getEntity(request.sourceNodeId());
    DiagramNodeEntity target = diagramNodeService.getEntity(request.targetNodeId());
    validateNodesBelongToDiagram(diagram, source, target);
    return DtoMapper.toDiagramEdge(createEntity(diagram, source, target, request.label(), request.type(), request.styleJson()));
  }

  DiagramEdgeEntity createEntity(
      DiagramEntity diagram,
      DiagramNodeEntity source,
      DiagramNodeEntity target,
      String label,
      String type,
      String styleJson
  ) {
    validateNodesBelongToDiagram(diagram, source, target);
    DiagramEdgeEntity edge = new DiagramEdgeEntity();
    edge.setDiagram(diagram);
    edge.setSourceNode(source);
    edge.setTargetNode(target);
    edge.setLabel(label);
    edge.setType(type == null || type.isBlank() ? "arrow" : type);
    edge.setStyleJson(styleJson);
    return diagramEdgeRepository.save(edge);
  }

  @Transactional(readOnly = true)
  public List<DiagramEdgeDtos.DiagramEdgeResponse> listByDiagram(Long diagramId) {
    diagramService.getEntity(diagramId);
    return diagramEdgeRepository.findByDiagramIdOrderByIdAsc(diagramId)
        .stream()
        .map(DtoMapper::toDiagramEdge)
        .toList();
  }

  public DiagramEdgeDtos.DiagramEdgeResponse update(Long id, DiagramEdgeDtos.UpdateDiagramEdgeRequest request) {
    DiagramEdgeEntity edge = getEntity(id);
    DiagramNodeEntity source = request.sourceNodeId() == null ? edge.getSourceNode() : diagramNodeService.getEntity(request.sourceNodeId());
    DiagramNodeEntity target = request.targetNodeId() == null ? edge.getTargetNode() : diagramNodeService.getEntity(request.targetNodeId());
    validateNodesBelongToDiagram(edge.getDiagram(), source, target);
    edge.setSourceNode(source);
    edge.setTargetNode(target);
    if (request.label() != null) edge.setLabel(request.label());
    if (request.type() != null) edge.setType(request.type());
    if (request.styleJson() != null) edge.setStyleJson(request.styleJson());
    return DtoMapper.toDiagramEdge(edge);
  }

  public void delete(Long id) {
    diagramEdgeRepository.delete(getEntity(id));
  }

  List<DiagramEdgeEntity> entitiesByDiagram(Long diagramId) {
    return diagramEdgeRepository.findByDiagramIdOrderByIdAsc(diagramId);
  }

  private DiagramEdgeEntity getEntity(Long id) {
    return diagramEdgeRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Diagram edge not found."));
  }

  private void validateNodesBelongToDiagram(DiagramEntity diagram, DiagramNodeEntity source, DiagramNodeEntity target) {
    if (source.getId().equals(target.getId())) {
      throw ApiException.badRequest("Diagram edge source and target must be different nodes.");
    }
    if (!source.getDiagram().getId().equals(diagram.getId()) || !target.getDiagram().getId().equals(diagram.getId())) {
      throw ApiException.badRequest("Diagram edges must reference nodes from the same diagram.");
    }
  }
}
