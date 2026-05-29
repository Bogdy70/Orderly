package com.orderly.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public final class UserDtos {
  private UserDtos() {
  }

  public record CreateUserRequest(
      @Email @NotBlank String email,
      @NotBlank String username,
      String passwordHash,
      String authProviderId
  ) {
  }

  public record UpdateUserRequest(
      String email,
      String username,
      String passwordHash,
      String authProviderId
  ) {
  }

  public record UserResponse(
      Long id,
      String email,
      String username,
      String authProviderId,
      Instant createdAt,
      Instant updatedAt
  ) {
  }
}
