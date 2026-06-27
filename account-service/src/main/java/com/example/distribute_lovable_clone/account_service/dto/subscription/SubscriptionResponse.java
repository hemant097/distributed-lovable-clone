package com.example.distribute_lovable_clone.account_service.dto.subscription;

import com.example.distributelovableclone.commonlib.dto.PlanDto;

import java.time.Instant;

public record SubscriptionResponse(
        PlanDto plan,
        String status,
        Instant currentPeriodEnd,
        Long tokensUsedThisCycle
) {
}
