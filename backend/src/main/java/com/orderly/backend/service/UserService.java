package com.orderly.backend.service;

import com.orderly.backend.dto.UserDtos;
import com.orderly.backend.entity.UserEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.UserRepository;
import com.orderly.backend.security.SecurityIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
  private final UserRepository userRepository;
  private final SecurityIdentityService securityIdentityService;

  public UserService(UserRepository userRepository, SecurityIdentityService securityIdentityService) {
    this.userRepository = userRepository;
    this.securityIdentityService = securityIdentityService;
  }

  public UserDtos.UserResponse create(UserDtos.CreateUserRequest request) {
    String keycloakId = securityIdentityService.requireKeycloakSubject();
    String email = resolveEmail(request.email());
    String username = resolveUsername(request.username());

    if (userRepository.existsByKeycloakId(keycloakId)) {
      return DtoMapper.toUser(userRepository.findByKeycloakId(keycloakId)
          .orElseThrow(() -> ApiException.conflict("A user with the same Keycloak ID already exists.")));
    }

    if (userRepository.existsByEmail(email)) {
      UserEntity user = userRepository.findByEmail(email)
          .orElseThrow(() -> ApiException.conflict("A user with the same email already exists."));
      if (user.getKeycloakId() != null && !user.getKeycloakId().isBlank() && !user.getKeycloakId().equals(keycloakId)) {
        throw ApiException.conflict("A user with the same email already exists.");
      }
      user.setKeycloakId(keycloakId);
      if (!user.getUsername().equals(username)) {
        user.setUsername(username);
      }
      return DtoMapper.toUser(userRepository.save(user));
    }

    if (userRepository.existsByUsername(username)) {
      throw ApiException.conflict("A user with the same username already exists.");
    }

    UserEntity user = new UserEntity();
    user.setKeycloakId(keycloakId);
    user.setEmail(email);
    user.setUsername(username);
    return DtoMapper.toUser(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public UserDtos.UserResponse get(Long id) {
    return DtoMapper.toUser(getEntity(id));
  }

  public UserDtos.UserResponse currentUser() {
    return DtoMapper.toUser(getOrCreateCurrentUserEntity());
  }

  public UserDtos.UserResponse update(Long id, UserDtos.UpdateUserRequest request) {
    UserEntity user = getEntity(id);
    if (request.email() != null) user.setEmail(request.email());
    if (request.username() != null) user.setUsername(request.username());
    return DtoMapper.toUser(user);
  }

  public void delete(Long id) {
    UserEntity user = getEntity(id);
    userRepository.delete(user);
  }

  UserEntity getEntity(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("User not found."));
  }

  public UserEntity getCurrentUserEntity() {
    return getOrCreateCurrentUserEntity();
  }

  private UserEntity getOrCreateCurrentUserEntity() {
    String keycloakId = securityIdentityService.requireKeycloakSubject();
    return userRepository.findByKeycloakId(keycloakId)
        .orElseGet(() -> findOrCreateByVerifiedEmail(keycloakId));
  }

  private UserEntity findOrCreateByVerifiedEmail(String keycloakId) {
    String email = securityIdentityService.findEmailClaim()
        .map(value -> value.trim().toLowerCase())
        .filter(value -> !value.isBlank())
        .orElseThrow(() -> ApiException.badRequest("Authenticated Keycloak account is missing an email claim."));
    String preferredUsername = securityIdentityService.findPreferredUsernameClaim()
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .orElseGet(() -> email.substring(0, email.indexOf('@')));

    return userRepository.findByEmail(email)
        .map(user -> {
          user.setKeycloakId(keycloakId);
          if (user.getUsername() == null || user.getUsername().isBlank()) {
            user.setUsername(preferredUsername);
          }
          return userRepository.save(user);
        })
        .orElseGet(() -> {
          String username = preferredUsername;
          if (userRepository.existsByUsername(username)) {
            username = username + "-" + keycloakId.substring(0, Math.min(8, keycloakId.length()));
          }
          UserEntity user = new UserEntity();
          user.setKeycloakId(keycloakId);
          user.setEmail(email);
          user.setUsername(username);
          return userRepository.save(user);
        });
  }

  private String resolveEmail(String requestedEmail) {
    String email = requestedEmail == null ? "" : requestedEmail.trim().toLowerCase();
    if (email.isBlank()) {
      throw ApiException.badRequest("Email is required.");
    }

    securityIdentityService.findEmailClaim().ifPresent(tokenEmail -> {
      if (!tokenEmail.equalsIgnoreCase(email)) {
        throw ApiException.badRequest("Email must match the authenticated Keycloak account.");
      }
    });
    return email;
  }

  private String resolveUsername(String requestedUsername) {
    if (requestedUsername != null && !requestedUsername.isBlank()) {
      return requestedUsername.trim();
    }
    return securityIdentityService.findPreferredUsernameClaim()
        .orElseThrow(() -> ApiException.badRequest("Username is required when preferred_username is not present in the JWT."));
  }
}
