package com.example.distributed_lovable_clone.account_service.dto.subscription;

import com.example.distributed_lovable_clone.commonlib.dto.PlanDto;

import java.time.Instant;

public record SubscriptionResponse(
        PlanDto plan,
        String status,
        Instant currentPeriodEnd,
        Long tokensUsedThisCycle
) {
}
