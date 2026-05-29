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

  @Transactional(readOnly = true)
  public UserDtos.UserResponse currentUser() {
    return DtoMapper.toUser(getCurrentUserEntity());
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
    String keycloakId = securityIdentityService.requireKeycloakSubject();
    return userRepository.findByKeycloakId(keycloakId)
        .orElseThrow(() -> ApiException.notFound("Local Orderly user not found. Create it with POST /api/users first."));
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
