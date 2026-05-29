package com.orderly.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orderly.backend.dto.UserDtos;
import com.orderly.backend.entity.UserEntity;
import com.orderly.backend.exception.ApiException;
import com.orderly.backend.repository.UserRepository;
import com.orderly.backend.security.SecurityIdentityService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private UserRepository userRepository;

  @Mock
  private SecurityIdentityService securityIdentityService;

  @Test
  void connectsCurrentKeycloakAccountWithPostedEmailAndUsername() {
    UserService userService = new UserService(userRepository, securityIdentityService);
    when(securityIdentityService.requireKeycloakSubject()).thenReturn("keycloak-person-sub");
    when(securityIdentityService.findEmailClaim()).thenReturn(Optional.of("person@orderly.local"));
    when(userRepository.existsByKeycloakId("keycloak-person-sub")).thenReturn(false);
    when(userRepository.existsByEmail("person@orderly.local")).thenReturn(false);
    when(userRepository.existsByUsername("local-person")).thenReturn(false);
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserDtos.UserResponse response = userService.create(
        new UserDtos.CreateUserRequest("person@orderly.local", "local-person")
    );

    assertThat(response.email()).isEqualTo("person@orderly.local");
    assertThat(response.username()).isEqualTo("local-person");
    assertThat(response.keycloakId()).isEqualTo("keycloak-person-sub");
    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getKeycloakId()).isEqualTo("keycloak-person-sub");
  }

  @Test
  void returnsExistingUserWhenKeycloakAccountIsAlreadyLinked() {
    UserService userService = new UserService(userRepository, securityIdentityService);
    when(securityIdentityService.requireKeycloakSubject()).thenReturn("keycloak-person-sub");
    when(securityIdentityService.findEmailClaim()).thenReturn(Optional.of("person@orderly.local"));
    when(userRepository.existsByKeycloakId("keycloak-person-sub")).thenReturn(true);
    UserEntity existing = new UserEntity();
    existing.setEmail("person@orderly.local");
    existing.setUsername("person");
    existing.setKeycloakId("keycloak-person-sub");
    when(userRepository.findByKeycloakId("keycloak-person-sub")).thenReturn(Optional.of(existing));

    UserDtos.UserResponse response = userService.create(new UserDtos.CreateUserRequest("person@orderly.local", "person"));

    assertThat(response.email()).isEqualTo("person@orderly.local");
    assertThat(response.username()).isEqualTo("person");
    assertThat(response.keycloakId()).isEqualTo("keycloak-person-sub");
  }

  @Test
  void linksExistingLocalUserByEmail() {
    UserService userService = new UserService(userRepository, securityIdentityService);
    UserEntity existing = new UserEntity();
    existing.setEmail("demo@orderly.local");
    existing.setUsername("demo");
    existing.setKeycloakId(null);
    when(securityIdentityService.requireKeycloakSubject()).thenReturn("demo");
    when(securityIdentityService.findEmailClaim()).thenReturn(Optional.of("demo@orderly.local"));
    when(userRepository.existsByKeycloakId("demo")).thenReturn(false);
    when(userRepository.existsByEmail("demo@orderly.local")).thenReturn(true);
    when(userRepository.findByEmail("demo@orderly.local")).thenReturn(Optional.of(existing));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserDtos.UserResponse response = userService.create(new UserDtos.CreateUserRequest("demo@orderly.local", "demo"));

    assertThat(response.email()).isEqualTo("demo@orderly.local");
    assertThat(response.username()).isEqualTo("demo");
    assertThat(response.keycloakId()).isEqualTo("demo");
  }

  @Test
  void rejectsPostedEmailThatDoesNotMatchKeycloakEmail() {
    UserService userService = new UserService(userRepository, securityIdentityService);
    when(securityIdentityService.requireKeycloakSubject()).thenReturn("keycloak-person-sub");
    when(securityIdentityService.findEmailClaim()).thenReturn(Optional.of("person@orderly.local"));

    assertThatThrownBy(() -> userService.create(new UserDtos.CreateUserRequest("other@orderly.local", "person")))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Email must match");
  }

  @Test
  void returnsCurrentLocalUserByKeycloakSubject() {
    UserService userService = new UserService(userRepository, securityIdentityService);
    UserEntity existing = new UserEntity();
    existing.setEmail("demo@orderly.local");
    existing.setUsername("demo");
    existing.setKeycloakId("keycloak-demo-sub");
    when(securityIdentityService.requireKeycloakSubject()).thenReturn("keycloak-demo-sub");
    when(userRepository.findByKeycloakId("keycloak-demo-sub")).thenReturn(Optional.of(existing));

    UserDtos.UserResponse response = userService.currentUser();

    assertThat(response.email()).isEqualTo("demo@orderly.local");
    assertThat(response.username()).isEqualTo("demo");
    assertThat(response.keycloakId()).isEqualTo("keycloak-demo-sub");
  }

  @Test
  void currentUserCreatesLocalUserWhenMissing() {
    UserService userService = new UserService(userRepository, securityIdentityService);
    when(securityIdentityService.requireKeycloakSubject()).thenReturn("new-sub");
    when(securityIdentityService.findEmailClaim()).thenReturn(Optional.of("new@orderly.local"));
    when(securityIdentityService.findPreferredUsernameClaim()).thenReturn(Optional.of("new-user"));
    when(userRepository.findByKeycloakId("new-sub")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("new@orderly.local")).thenReturn(Optional.empty());
    when(userRepository.existsByUsername("new-user")).thenReturn(false);
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserDtos.UserResponse response = userService.currentUser();

    assertThat(response.email()).isEqualTo("new@orderly.local");
    assertThat(response.username()).isEqualTo("new-user");
    assertThat(response.keycloakId()).isEqualTo("new-sub");
  }
}
