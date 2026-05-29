package com.orderly.backend.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") String username,
    @NotBlank @Size(min = 8, max = 100) String password
) {
}
