package com.orderly.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class EmailVerifiedFilterTest {
  private final OrderlySecurityProperties properties = new OrderlySecurityProperties();
  private final EmailVerifiedFilter filter = new EmailVerifiedFilter(properties, new ObjectMapper().registerModule(new JavaTimeModule()));

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void rejectsApiRequestsWhenJwtEmailIsNotVerified() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt(false, "demo@orderly.local")));
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/spaces");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentAsString()).contains("verified email");
    verify(chain, never()).doFilter(request, response);
  }

  @Test
  void rejectsApiRequestsWhenJwtEmailIsMissing() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt(true, null)));
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/spaces");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(403);
    verify(chain, never()).doFilter(request, response);
  }

  @Test
  void allowsApiRequestsWhenJwtEmailIsVerified() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt(true, "demo@orderly.local")));
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/spaces");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void skipsNonApiRequests() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt(false, "demo@orderly.local")));
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui.html");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  private Jwt jwt(boolean emailVerified, String email) {
    Map<String, Object> claims = new java.util.HashMap<>();
    claims.put("sub", "user-1");
    claims.put("preferred_username", "demo");
    claims.put("email_verified", emailVerified);
    if (email != null) {
      claims.put("email", email);
    }

    return new Jwt(
        "token",
        Instant.parse("2026-05-29T10:00:00Z"),
        Instant.parse("2026-05-29T11:00:00Z"),
        Map.of("alg", "none"),
        claims
    );
  }
}
