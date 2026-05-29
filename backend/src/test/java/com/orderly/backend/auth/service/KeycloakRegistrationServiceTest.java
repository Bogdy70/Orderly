package com.orderly.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.orderly.backend.auth.dto.request.RegisterRequest;
import com.orderly.backend.entity.UserEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KeycloakRegistrationServiceTest {
  private final RestClient.Builder restClientBuilder = RestClient.builder();
  private final MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
  private final UserRepository userRepository = mock(UserRepository.class);
  private final KeycloakRegistrationService service = new KeycloakRegistrationService(
      restClientBuilder,
      userRepository,
      "http://keycloak",
      "orderly",
      "admin",
      "admin"
  );

  @Test
  void registerCreatesKeycloakUserAndLocalUser() {
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(userRepository.existsByUsername(anyString())).thenReturn(false);

    server.expect(once(), requestTo("http://keycloak/realms/master/protocol/openid-connect/token"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess("{\"access_token\":\"admin-token\"}", MediaType.APPLICATION_JSON));
    server.expect(once(), requestTo("http://keycloak/admin/realms/orderly/users"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
        .andRespond(withStatus(HttpStatus.CREATED)
            .location(java.net.URI.create("http://keycloak/admin/realms/orderly/users/keycloak-123")));
    server.expect(once(), requestTo("http://keycloak/admin/realms/orderly/users/keycloak-123/reset-password"))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
        .andRespond(withStatus(HttpStatus.NO_CONTENT));
    server.expect(once(), requestTo("http://keycloak/admin/realms/orderly/users/keycloak-123"))
        .andExpect(method(HttpMethod.PUT))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
        .andRespond(withStatus(HttpStatus.NO_CONTENT));

    service.register(new RegisterRequest("New.User@Example.com", "new-user", "password"));

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getKeycloakId()).isEqualTo("keycloak-123");
    assertThat(userCaptor.getValue().getEmail()).isEqualTo("new.user@example.com");
    assertThat(userCaptor.getValue().getUsername()).isEqualTo("new-user");
    server.verify();
  }

  @Test
  void registerDoesNotCallKeycloakWhenLocalEmailExists() {
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.register(new RegisterRequest("taken@example.com", "taken", "password")))
        .isInstanceOf(ApiException.class)
        .hasMessage("A user with the same email already exists.");

    verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    server.verify();
  }
}
