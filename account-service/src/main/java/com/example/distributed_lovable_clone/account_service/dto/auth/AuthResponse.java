package com.example.distributed_lovable_clone.account_service.dto.auth;

public record AuthResponse(
        String token,
        UserProfileResponse user) {
}
