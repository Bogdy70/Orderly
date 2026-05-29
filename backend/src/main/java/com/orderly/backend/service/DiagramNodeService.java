package com.orderly.backend.service;

import com.orderly.backend.dto.DiagramNodeDtos;
import com.orderly.backend.entity.DiagramEntity;
import com.orderly.backend.entity.DiagramNodeEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.DiagramNodeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiagramNodeService {
  private final DiagramNodeRepository diagramNodeRepository;
  private final DiagramService diagramService;
  private final ValidationService validationService;

  public DiagramNodeService(
      DiagramNodeRepository diagramNodeRepository,
      DiagramService diagramService,
      ValidationService validationService
  ) {
    this.diagramNodeRepository = diagramNodeRepository;
    this.diagramService = diagramService;
    this.validationService = validationService;
  }

  public DiagramNodeDtos.DiagramNodeResponse create(Long diagramId, DiagramNodeDtos.CreateDiagramNodeRequest request) {
    DiagramEntity diagram = diagramService.getEntity(diagramId);
    DiagramNodeEntity node = createEntity(
        diagram,
        request.type(),
        request.label(),
        request.x(),
        request.y(),
        request.width(),
        request.height(),
        request.styleJson(),
        request.dataJson()
    );
    return DtoMapper.toDiagramNode(node);
  }

  DiagramNodeEntity createEntity(
      DiagramEntity diagram,
      String type,
      String label,
      Double x,
      Double y,
      Double width,
      Double height,
      String styleJson,
      String dataJson
  ) {
    validationService.requirePositiveDimensions(width, height);
    DiagramNodeEntity node = new DiagramNodeEntity();
    node.setDiagram(diagram);
    node.setType(type == null || type.isBlank() ? "default" : type);
    node.setLabel(label);
    node.setX(x);
    node.setY(y);
    node.setWidth(width);
    node.setHeight(height);
    node.setStyleJson(styleJson);
    node.setDataJson(dataJson);
    return diagramNodeRepository.save(node);
  }

  @Transactional(readOnly = true)
  public List<DiagramNodeDtos.DiagramNodeResponse> listByDiagram(Long diagramId) {
    diagramService.getEntity(diagramId);
    return diagramNodeRepository.findByDiagramIdOrderByIdAsc(diagramId)
        .stream()
        .map(DtoMapper::toDiagramNode)
        .toList();
  }

  public DiagramNodeDtos.DiagramNodeResponse update(Long id, DiagramNodeDtos.UpdateDiagramNodeRequest request) {
    DiagramNodeEntity node = getEntity(id);
    if (request.type() != null) node.setType(request.type());
    if (request.label() != null) node.setLabel(request.label());
    if (request.x() != null) node.setX(request.x());
    if (request.y() != null) node.setY(request.y());
    if (request.width() != null || request.height() != null) {
      Double width = request.width() == null ? node.getWidth() : request.width();
      Double height = request.height() == null ? node.getHeight() : request.height();
      validationService.requirePositiveDimensions(width, height);
      node.setWidth(width);
      node.setHeight(height);
    }
    if (request.styleJson() != null) node.setStyleJson(request.styleJson());
    if (request.dataJson() != null) node.setDataJson(request.dataJson());
    return DtoMapper.toDiagramNode(node);
  }

  public void delete(Long id) {
    diagramNodeRepository.delete(getEntity(id));
  }

  List<DiagramNodeEntity> entitiesByDiagram(Long diagramId) {
    return diagramNodeRepository.findByDiagramIdOrderByIdAsc(diagramId);
  }

  DiagramNodeEntity getEntity(Long id) {
    return diagramNodeRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Diagram node not found."));
  }
}
