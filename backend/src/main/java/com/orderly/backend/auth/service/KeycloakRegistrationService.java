package com.orderly.backend.auth.service;

import com.orderly.backend.auth.dto.request.RegisterRequest;
import com.orderly.backend.entity.UserEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.UserRepository;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class KeycloakRegistrationService {
  private final RestClient restClient;
  private final UserRepository userRepository;
  private final String serverUrl;
  private final String realm;
  private final String adminUsername;
  private final String adminPassword;

  public KeycloakRegistrationService(
      RestClient.Builder restClientBuilder,
      UserRepository userRepository,
      @Value("${keycloak.server-url}") String serverUrl,
      @Value("${keycloak.realm}") String realm,
      @Value("${keycloak.admin.username:admin}") String adminUsername,
      @Value("${keycloak.admin.password:admin}") String adminPassword
  ) {
    this.restClient = restClientBuilder.build();
    this.userRepository = userRepository;
    this.serverUrl = trimTrailingSlash(serverUrl);
    this.realm = realm;
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
  }

  public void register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase();
    String username = request.username().trim();

    if (userRepository.existsByEmail(email)) {
      throw ApiException.conflict("A user with the same email already exists.");
    }
    if (userRepository.existsByUsername(username)) {
      throw ApiException.conflict("A user with the same username already exists.");
    }

    String adminToken = fetchAdminToken();

    try {
      var response = restClient.post()
          .uri(URI.create(serverUrl + "/admin/realms/" + realm + "/users"))
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
          .contentType(MediaType.APPLICATION_JSON)
          .body(Map.of(
              "username", username,
              "email", email,
              "enabled", true,
              "emailVerified", true,
              "requiredActions", List.of(),
              "credentials", List.of(Map.of(
                  "type", "password",
                  "value", request.password(),
                  "temporary", false
              ))
          ))
          .retrieve()
          .toBodilessEntity();

      String keycloakId = extractCreatedUserId(response.getHeaders().getLocation());
      UserEntity user = new UserEntity();
      user.setKeycloakId(keycloakId);
      user.setEmail(email);
      user.setUsername(username);
      userRepository.save(user);
    } catch (RestClientResponseException exception) {
      if (exception.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
        throw ApiException.conflict("An account with this email or username already exists.");
      }
      throw ApiException.badRequest("Could not create the Keycloak account.");
    }
  }

  private String fetchAdminToken() {
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", "admin-cli");
    form.add("grant_type", "password");
    form.add("username", adminUsername);
    form.add("password", adminPassword);

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restClient.post()
          .uri(URI.create(serverUrl + "/realms/master/protocol/openid-connect/token"))
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(form)
          .retrieve()
          .body(Map.class);

      Object accessToken = response == null ? null : response.get("access_token");
      if (accessToken instanceof String token && !token.isBlank()) {
        return token;
      }
    } catch (RestClientResponseException exception) {
      throw ApiException.badRequest("Could not authenticate with Keycloak admin.");
    }

    throw ApiException.badRequest("Keycloak admin token was missing.");
  }

  private String extractCreatedUserId(URI location) {
    if (location == null) {
      throw ApiException.badRequest("Keycloak did not return the created user id.");
    }
    String path = location.getPath();
    int index = path.lastIndexOf('/');
    if (index < 0 || index == path.length() - 1) {
      throw ApiException.badRequest("Keycloak returned an invalid created user location.");
    }
    return path.substring(index + 1);
  }

  private String trimTrailingSlash(String value) {
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }
}
