package com.example.distributed_lovable_clone.account_service.dto.subscription;

import com.example.distributed_lovable_clone.common_lib.dto.PlanDto;

import java.time.Instant;

public record SubscriptionResponse(
        PlanDto plan,
        String status,
        Instant currentPeriodEnd,
        Long tokensUsedThisCycle
) {
}
