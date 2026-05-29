package com.orderly.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.orderly.backend.exception.ApiException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityIdentityServiceTest {
  private final SecurityIdentityService securityIdentityService = new SecurityIdentityService();

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void fallsBackToPreferredUsernameWhenJwtSubjectIsMissing() {
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwtWithoutSubject(), Collections.emptyList()));

    assertThat(securityIdentityService.requireKeycloakSubject()).isEqualTo("demo");
  }

  @Test
  void fallsBackToEmailWhenJwtSubjectAndPreferredUsernameAreMissing() {
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwtWithoutSubjectAndUsername(), Collections.emptyList()));

    assertThat(securityIdentityService.requireKeycloakSubject()).isEqualTo("demo@orderly.local");
  }

  private Jwt jwtWithoutSubject() {
    return new Jwt(
        "token",
        Instant.parse("2026-05-29T10:00:00Z"),
        Instant.parse("2026-05-29T11:00:00Z"),
        Map.of("alg", "none"),
        Map.of(
            "preferred_username", "demo",
            "email", "demo@orderly.local"
        )
    );
  }

  private Jwt jwtWithoutSubjectAndUsername() {
    return new Jwt(
        "token",
        Instant.parse("2026-05-29T10:00:00Z"),
        Instant.parse("2026-05-29T11:00:00Z"),
        Map.of("alg", "none"),
        Map.of(
            "email", "demo@orderly.local"
        )
    );
  }
}