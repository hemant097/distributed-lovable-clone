package com.example.distribute_lovable_clone.account_service.service.impl;


import com.example.distribute_lovable_clone.account_service.dto.subscription.SubscriptionResponse;
import com.example.distribute_lovable_clone.account_service.entity.Plan;
import com.example.distribute_lovable_clone.account_service.entity.Subscription;
import com.example.distribute_lovable_clone.account_service.entity.User;
import com.example.distribute_lovable_clone.account_service.mapper.SubscriptionMapper;
import com.example.distribute_lovable_clone.account_service.repository.PlanRepository;
import com.example.distribute_lovable_clone.account_service.repository.SubscriptionRepository;
import com.example.distribute_lovable_clone.account_service.repository.UserRepository;
import com.example.distribute_lovable_clone.account_service.service.SubscriptionService;
import com.example.distributelovableclone.commonlib.dto.PlanDto;
import com.example.distributelovableclone.commonlib.enums.SubscriptionStatus;
import com.example.distributelovableclone.commonlib.errors.ResourceNotFoundException;
import com.example.distributelovableclone.commonlib.security.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

import static com.example.distributelovableclone.commonlib.enums.SubscriptionStatus.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final AuthUtil authUtil;
    private final SubscriptionRepository subscriptionRepo;
    private final SubscriptionMapper subscriptionMapper;
//    private final ProjectMemberRepository projectMemberRepo;
    private final UserRepository userRepo;
    private final PlanRepository planRepo;
    final int FREE_TIER_PROJECTS_ALLOWED = 100;

    @Override
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtil.getCurrentUserId();

        Subscription subscription =  subscriptionRepo.findByUserIdAndStatusIn(userId, Set.of(ACTIVE,PAST_DUE,TRIALING))
                .orElse(new Subscription());

        return subscriptionMapper.toSubscriptionResponse(subscription);

    }

    @Override
    public void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId) {
            boolean exists = subscriptionRepo.existsByStripeSubscriptionId(subscriptionId);

            if(exists) return;

            User user = getUser(userId);
            Plan plan = getPlan(planId);

            Subscription subscription = Subscription.builder()
                    .user(user)
                    .plan(plan)
                    .stripeSubscriptionId(subscriptionId)
                    .status(INCOMPLETE)
                    .build();

            log.info("Subscription activated for user:{}, with plan:{} ",user.getUsername(), plan.getName());
            subscriptionRepo.save(subscription);

    }

    @Override
    @Transactional
    public void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus subscriptionStatus, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        boolean hasSubscriptionUpdated = false;

        if(subscriptionStatus!=null && subscriptionStatus!=subscription.getStatus()){
            subscription.setStatus(subscriptionStatus);
            log.info("subscription status changed to : {}",subscriptionStatus);
            hasSubscriptionUpdated = true;
        }

        if(periodStart!=null && periodStart!=subscription.getCurrentPeriodStart()){
            subscription.setCurrentPeriodStart(periodStart);
            log.info("start period changed to : {}",periodStart);
            hasSubscriptionUpdated = true;
        }

        if(periodEnd!=null && periodEnd!=subscription.getCurrentPeriodEnd()){
            subscription.setCurrentPeriodStart(periodEnd);
            log.info("end period changed to : {}",periodEnd);
            hasSubscriptionUpdated = true;
        }

        if(cancelAtPeriodEnd!=null && cancelAtPeriodEnd!=subscription.getCancelAtPeriodEnd()) {
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            log.info("set_cancel_at_period_end changed to : {}",cancelAtPeriodEnd);
            hasSubscriptionUpdated = true;
        }

        if(planId!=null && !planId.equals(subscription.getPlan().getId())){
            Plan newPlan = getPlan(planId);
            subscription.setPlan(newPlan);
            hasSubscriptionUpdated = true;
        }

        if(hasSubscriptionUpdated){
            log.debug("Subscription has been updated: {}",gatewaySubscriptionId);
            subscriptionRepo.save(subscription);
        }

    }

    @Override
    public void cancelSubscription(String gatewaySubscriptionId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        if(subscription.getStatus() == CANCELLED){
            log.debug("Subscription is already cancelled, gatewaySubscriptionId:{}",gatewaySubscriptionId);
            return;
        }
        subscription.setStatus(CANCELLED);
        subscriptionRepo.save(subscription);

    }

    @Override
    public void renewSubscriptionPeriod(String gatewaySubscriptionId, Instant periodStart, Instant periodEnd) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        Instant newStart = periodStart!=null ? periodStart : subscription.getCurrentPeriodEnd();
        subscription.setCurrentPeriodStart(newStart);
        subscription.setCurrentPeriodEnd(periodEnd);

        if(subscription.getStatus() == PAST_DUE || subscription.getStatus() == INCOMPLETE){
            subscription.setStatus(ACTIVE);
        }

        log.info("Subscription renewed, starting from:{}, till {}",newStart, periodEnd);
        subscriptionRepo.save(subscription);

    }

//    public boolean canCreateNewProject(){
//
//        Long userId = authUtil.getCurrentUserId();
//        SubscriptionResponse currentSubscription = getCurrentSubscription();
//
//        int countOfOwnedProjects = projectMemberRepo.countProjectsOwnedByUser(userId);
//
//        //if on FREE TIER
//        if(currentSubscription.plan() == null){
//            return countOfOwnedProjects < FREE_TIER_PROJECTS_ALLOWED;
//        }
//
//        //if on subscription plan
//        return countOfOwnedProjects < currentSubscription.plan().maxProjects();
//    }

    @Override
    public void markSubscriptionPastDue(String gatewaySubscriptionId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        if(subscription.getStatus() == PAST_DUE){
            log.debug("Subscription is already past due, gatewaySubscriptionId:{}",gatewaySubscriptionId);
            return;
        }

        subscription.setStatus(PAST_DUE);
        subscriptionRepo.save(subscription);

        //we can notify user via email later

    }

    @Override
    public PlanDto getCurrentSubscribedPlanByUser() {
        SubscriptionResponse subscriptionResponse = getCurrentSubscription();
        return subscriptionResponse.plan();
    }

    /// Utility methods

    private User getUser(Long userId){
        return userRepo.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user",userId.toString()));
    }

    private Plan getPlan(Long planId){
        return planRepo.findById(planId).orElseThrow(()-> new ResourceNotFoundException("plan",planId.toString()));
    }

    private Subscription getSubscription(String gatewaySubscriptionId) {
        return subscriptionRepo.findByStripeSubscriptionId(gatewaySubscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("subscription", gatewaySubscriptionId));
    }


}
