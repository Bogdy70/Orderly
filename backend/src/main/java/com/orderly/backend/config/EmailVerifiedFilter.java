package com.orderly.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderly.backend.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class EmailVerifiedFilter extends OncePerRequestFilter {
  private final OrderlySecurityProperties securityProperties;
  private final ObjectMapper objectMapper;

  public EmailVerifiedFilter(OrderlySecurityProperties securityProperties, ObjectMapper objectMapper) {
    this.securityProperties = securityProperties;
    this.objectMapper = objectMapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return !path.startsWith("/api/") || "OPTIONS".equalsIgnoreCase(request.getMethod());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (!securityProperties.isRequireVerifiedEmail()) {
      filterChain.doFilter(request, response);
      return;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
      filterChain.doFilter(request, response);
      return;
    }

    Jwt jwt = jwtAuthentication.getToken();
    Boolean emailVerified = jwt.getClaim("email_verified");
    String email = jwt.getClaimAsString("email");

    if (Boolean.TRUE.equals(emailVerified) && email != null && !email.isBlank()) {
      filterChain.doFilter(request, response);
      return;
    }

    ApiError error = new ApiError(
        Instant.now(),
        HttpStatus.FORBIDDEN.value(),
        HttpStatus.FORBIDDEN.getReasonPhrase(),
        "A verified email address is required to use the Orderly API.",
        Map.of("email_verified", "must be true")
    );

    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), error);
  }
}
