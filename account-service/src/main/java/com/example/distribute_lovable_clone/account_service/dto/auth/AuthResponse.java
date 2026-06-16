package com.example.distribute_lovable_clone.account_service.dto.auth;

public record AuthResponse(
        String token,
        UserProfileResponse user) {
}
