package com.orderly.backend.security;

import com.orderly.backend.exception.ApiException;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class SecurityIdentityService {
  public String requireKeycloakSubject() {
    Authentication authentication = requireAuthentication();
    if (authentication.getPrincipal() instanceof Jwt jwt) {
      String subject = firstNonBlank(
          jwt.getSubject(),
          jwt.getClaimAsString("preferred_username"),
          jwt.getClaimAsString("username"),
          jwt.getClaimAsString("email")
      );
      if (subject == null) {
        throw ApiException.badRequest("JWT identity is missing.");
      }
      return subject;
    }

    String principalName = authentication.getName();
    if (principalName == null || principalName.isBlank() || "anonymousUser".equals(principalName)) {
      throw ApiException.badRequest("Authentication is required.");
    }
    return principalName;
  }

  public Optional<String> findEmailClaim() {
    return findJwt()
        .map(jwt -> jwt.getClaimAsString("email"))
        .filter(email -> !email.isBlank());
  }

  public Optional<String> findPreferredUsernameClaim() {
    return findJwt()
        .map(jwt -> {
          String preferredUsername = jwt.getClaimAsString("preferred_username");
          if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
          }
          return jwt.getClaimAsString("username");
        })
        .filter(username -> !username.isBlank());
  }

  private Authentication requireAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      throw ApiException.badRequest("Authentication is required.");
    }
    return authentication;
  }

  private Optional<Jwt> findJwt() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      return Optional.of(jwt);
    }
    return Optional.empty();
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
