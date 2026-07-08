package com.example.distributed_lovable_clone.account_service.dto.auth;

public record UserProfileResponse(
        Long id,
        String username,
        String name
        ) {
}
