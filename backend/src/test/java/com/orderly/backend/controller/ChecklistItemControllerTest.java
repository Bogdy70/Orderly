package com.orderly.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderly.backend.config.OrderlySecurityProperties;
import com.orderly.backend.dto.ChecklistItemDtos;
import com.orderly.backend.service.ChecklistItemService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChecklistItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChecklistItemControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private ChecklistItemService checklistItemService;

  @MockBean
  private OrderlySecurityProperties orderlySecurityProperties;

  @Test
  void createsChecklistItem() throws Exception {
    Instant now = Instant.parse("2026-05-29T10:00:00Z");
    ChecklistItemDtos.ChecklistItemResponse response =
        new ChecklistItemDtos.ChecklistItemResponse(7L, 3L, "Review plan", false, 1, now, now);
    when(checklistItemService.create(eq(3L), any(ChecklistItemDtos.CreateChecklistItemRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/blocks/3/checklist-items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ChecklistItemDtos.CreateChecklistItemRequest("Review plan", false, 1))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.blockId").value(3))
        .andExpect(jsonPath("$.text").value("Review plan"));
  }

  @Test
  void rejectsBlankChecklistText() throws Exception {
    mockMvc.perform(post("/api/blocks/3/checklist-items")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"text":"","done":false,"position":1}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.text").exists());
  }
}
