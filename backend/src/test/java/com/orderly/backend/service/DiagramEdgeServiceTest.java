package com.orderly.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orderly.backend.dto.DiagramEdgeDtos;
import com.orderly.backend.entity.DiagramEntity;
import com.orderly.backend.entity.DiagramNodeEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.DiagramEdgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DiagramEdgeServiceTest {
  @Mock
  private DiagramEdgeRepository diagramEdgeRepository;

  @Mock
  private DiagramService diagramService;

  @Mock
  private DiagramNodeService diagramNodeService;

  @InjectMocks
  private DiagramEdgeService diagramEdgeService;

  @Test
  void rejectsEdgesAcrossDifferentDiagrams() {
    DiagramEntity diagram = diagram(1L);
    DiagramNodeEntity source = node(10L, diagram);
    DiagramNodeEntity target = node(11L, diagram(2L));

    when(diagramService.getEntity(1L)).thenReturn(diagram);
    when(diagramNodeService.getEntity(10L)).thenReturn(source);
    when(diagramNodeService.getEntity(11L)).thenReturn(target);

    assertThatThrownBy(() -> diagramEdgeService.create(
        1L,
        new DiagramEdgeDtos.CreateDiagramEdgeRequest(10L, 11L, "bad edge", "arrow", "{}")
    ))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("same diagram");

    verify(diagramEdgeRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void rejectsSelfReferencingEdges() {
    DiagramEntity diagram = diagram(1L);
    DiagramNodeEntity source = node(10L, diagram);

    when(diagramService.getEntity(1L)).thenReturn(diagram);
    when(diagramNodeService.getEntity(10L)).thenReturn(source);

    assertThatThrownBy(() -> diagramEdgeService.create(
        1L,
        new DiagramEdgeDtos.CreateDiagramEdgeRequest(10L, 10L, "loop", "arrow", "{}")
    ))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("different nodes");

    verify(diagramEdgeRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  private DiagramEntity diagram(Long id) {
    DiagramEntity diagram = new DiagramEntity();
    ReflectionTestUtils.setField(diagram, "id", id);
    return diagram;
  }

  private DiagramNodeEntity node(Long id, DiagramEntity diagram) {
    DiagramNodeEntity node = new DiagramNodeEntity();
    ReflectionTestUtils.setField(node, "id", id);
    node.setDiagram(diagram);
    return node;
  }
}
