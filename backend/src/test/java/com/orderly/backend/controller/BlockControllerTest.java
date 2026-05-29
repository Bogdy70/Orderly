package com.orderly.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderly.backend.dto.BlockDtos;
import com.orderly.backend.entity.BlockType;
import com.orderly.backend.service.BlockConversionService;
import com.orderly.backend.service.BlockFullService;
import com.orderly.backend.service.BlockService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BlockController.class)
class BlockControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BlockService blockService;

  @MockBean
  private BlockFullService blockFullService;

  @MockBean
  private BlockConversionService blockConversionService;

  @Test
  void createsBlockOnNestedSpaceRoute() throws Exception {
    BlockDtos.BlockResponse response = blockResponse(10L, 4L, "checklist", "Today", 1);
    when(blockService.create(eq(4L), any(BlockDtos.CreateBlockRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/spaces/4/blocks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BlockDtos.CreateBlockRequest("checklist", "Today", 1))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.spaceId").value(4))
        .andExpect(jsonPath("$.type").value("checklist"));
  }

  @Test
  void rejectsCreateBlockWithoutType() throws Exception {
    mockMvc.perform(post("/api/spaces/4/blocks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"Missing type","position":1}
                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returnsFullBlock() throws Exception {
    BlockDtos.BlockResponse block = blockResponse(10L, 4L, "checklist", "Today", 1);
    BlockDtos.BlockFullResponse response = new BlockDtos.BlockFullResponse(block, List.of());
    when(blockFullService.getFull(10L)).thenReturn(response);

    mockMvc.perform(get("/api/blocks/10/full"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.block.id").value(10))
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void convertsBlockToTable() throws Exception {
    BlockDtos.BlockResponse block = blockResponse(11L, 4L, "table", "Today - Table", 2);
    when(blockConversionService.convert(10L, BlockType.TABLE)).thenReturn(new BlockDtos.BlockFullResponse(block, List.of()));

    mockMvc.perform(post("/api/blocks/10/convert/table"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.block.type").value("table"))
        .andExpect(jsonPath("$.block.title").value("Today - Table"));

    verify(blockConversionService).convert(10L, BlockType.TABLE);
  }

  @Test
  void updatesBlockMetadata() throws Exception {
    BlockDtos.BlockResponse response = blockResponse(10L, 4L, "checklist", "Later", 2);
    when(blockService.update(eq(10L), any(BlockDtos.UpdateBlockRequest.class))).thenReturn(response);

    mockMvc.perform(patch("/api/blocks/10")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new BlockDtos.UpdateBlockRequest("Later", 2))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Later"))
        .andExpect(jsonPath("$.position").value(2));
  }

  private BlockDtos.BlockResponse blockResponse(Long id, Long spaceId, String type, String title, Integer position) {
    Instant now = Instant.parse("2026-05-29T10:00:00Z");
    return new BlockDtos.BlockResponse(id, spaceId, type, title, position, now, now);
  }
}
