package com.orderly.backend.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderly.backend.auth.dto.request.RegisterRequest;
import com.orderly.backend.auth.service.KeycloakRegistrationService;
import com.orderly.backend.config.OrderlySecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private KeycloakRegistrationService keycloakRegistrationService;

  @MockBean
  private OrderlySecurityProperties orderlySecurityProperties;

  @Test
  void registersKeycloakAccount() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new RegisterRequest(
                "person@orderly.local",
                "person",
                "password"
            ))))
        .andExpect(status().isCreated());

    verify(keycloakRegistrationService).register(any(RegisterRequest.class));
  }

  @Test
  void rejectsInvalidRegistrationRequest() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"not-an-email","username":"","password":""}
                """))
        .andExpect(status().isBadRequest());
  }
}
