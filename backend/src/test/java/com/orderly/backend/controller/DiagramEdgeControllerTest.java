package com.orderly.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderly.backend.dto.DiagramEdgeDtos;
import com.orderly.backend.service.DiagramEdgeService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DiagramEdgeController.class)
class DiagramEdgeControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private DiagramEdgeService diagramEdgeService;

  @Test
  void createsDiagramEdge() throws Exception {
    Instant now = Instant.parse("2026-05-29T10:00:00Z");
    DiagramEdgeDtos.DiagramEdgeResponse response =
        new DiagramEdgeDtos.DiagramEdgeResponse(9L, 2L, 4L, 5L, "next", "arrow", "{}", now, now);
    when(diagramEdgeService.create(eq(2L), any(DiagramEdgeDtos.CreateDiagramEdgeRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/diagrams/2/edges")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new DiagramEdgeDtos.CreateDiagramEdgeRequest(4L, 5L, "next", "arrow", "{}"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.sourceNodeId").value(4))
        .andExpect(jsonPath("$.targetNodeId").value(5));
  }

  @Test
  void rejectsEdgeWithoutTargetNode() throws Exception {
    mockMvc.perform(post("/api/diagrams/2/edges")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"sourceNodeId":4,"label":"next","type":"arrow"}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.targetNodeId").exists());
  }
}
