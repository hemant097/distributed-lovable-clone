package com.example.distribute_lovable_clone.account_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @Email @NotBlank
        String username,
        @Size(min = 2, max = 30)
        String name,
        @Size(min = 4, max = 18)
        String password
) {
}
