package com.example.distribute_lovable_clone.account_service.service;



import com.example.distribute_lovable_clone.account_service.dto.subscription.SubscriptionResponse;
import com.example.distributelovableclone.commonlib.dto.PlanDto;
import com.example.distributelovableclone.commonlib.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();

    void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId);

    void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus subscriptionStatus, Instant periodStart, Instant periodEnd,
                            Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String gatewaySubscriptionId);

    void renewSubscriptionPeriod(String gatewaySubscriptionId, Instant periodStart, Instant periodEnd);

    void markSubscriptionPastDue(String gatewaySubscriptionId);

    PlanDto getCurrentSubscribedPlanByUser();

//    boolean canCreateNewProject();
}
