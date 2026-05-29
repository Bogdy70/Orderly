package com.orderly.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderly.backend.config.OrderlySecurityProperties;
import com.orderly.backend.dto.SpaceDtos;
import com.orderly.backend.entity.UserEntity;
import com.orderly.backend.service.SpaceService;
import com.orderly.backend.service.UserService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SpaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpaceControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private SpaceService spaceService;

  @MockBean
  private UserService userService;

  @MockBean
  private OrderlySecurityProperties orderlySecurityProperties;

  @Test
  void createsSpaceWithoutOwnerIdInRequestBody() throws Exception {
    UserEntity owner = new UserEntity();
    SpaceDtos.SpaceResponse response = spaceResponse(8L, 3L, "Planning");
    when(userService.getCurrentUserEntity()).thenReturn(owner);
    when(spaceService.create(any(SpaceDtos.CreateSpaceRequest.class), any(UserEntity.class))).thenReturn(response);

    mockMvc.perform(post("/api/spaces")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new SpaceDtos.CreateSpaceRequest(
                "Planning",
                "Personal planning space",
                "layout-dashboard",
                "#2563eb"
            ))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(8))
        .andExpect(jsonPath("$.ownerId").value(3))
        .andExpect(jsonPath("$.name").value("Planning"));
  }

  @Test
  void listsSpacesForAuthenticatedUserWithoutOwnerIdQueryParam() throws Exception {
    UserEntity owner = new UserEntity();
    SpaceDtos.SpaceResponse response = spaceResponse(8L, 3L, "Planning");
    when(userService.getCurrentUserEntity()).thenReturn(owner);
    when(spaceService.list(owner)).thenReturn(List.of(response));

    mockMvc.perform(get("/api/spaces"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(8))
        .andExpect(jsonPath("$[0].ownerId").value(3))
        .andExpect(jsonPath("$[0].name").value("Planning"));
  }

  private SpaceDtos.SpaceResponse spaceResponse(Long id, Long ownerId, String name) {
    Instant now = Instant.parse("2026-05-29T10:00:00Z");
    return new SpaceDtos.SpaceResponse(id, ownerId, name, "Description", "layout-dashboard", "#2563eb", now, now);
  }
}
