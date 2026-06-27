package com.example.distribute_lovable_clone.account_service.service.impl;

import com.example.distribute_lovable_clone.account_service.dto.subscription.CheckoutRequest;
import com.example.distribute_lovable_clone.account_service.dto.subscription.CheckoutResponse;
import com.example.distribute_lovable_clone.account_service.dto.subscription.PortalResponse;
import com.example.distribute_lovable_clone.account_service.entity.Plan;
import com.example.distribute_lovable_clone.account_service.entity.User;
import com.example.distribute_lovable_clone.account_service.repository.PlanRepository;
import com.example.distribute_lovable_clone.account_service.repository.UserRepository;
import com.example.distribute_lovable_clone.account_service.service.PaymentProcessor;
import com.example.distribute_lovable_clone.account_service.service.SubscriptionService;
import com.example.distributelovableclone.commonlib.enums.SubscriptionStatus;
import com.example.distributelovableclone.commonlib.errors.BadRequestException;
import com.example.distributelovableclone.commonlib.errors.ResourceNotFoundException;
import com.example.distributelovableclone.commonlib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {

    private final AuthUtil authUtil;
    private final PlanRepository planRepo;
    private final UserRepository userRepo;
    private final SubscriptionService subscriptionService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Plan plan = planRepo.findById(request.planId()).orElseThrow(() ->
                new ResourceNotFoundException("Plan",request.planId().toString()));

        Long userId = authUtil.getCurrentUserId();
        User user = getUser(userId);

        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(plan.getStripePriceId())
                            .setQuantity(1L)
                            .build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(SessionCreateParams.SubscriptionData.builder()
                                    .setBillingMode(
                                            SessionCreateParams.SubscriptionData.BillingMode.builder()
                                            .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                            .build()
                                        )
                                    .build()
                                    )
                .setSuccessUrl(frontendUrl +"/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl +"/cancel.html")
                .putMetadata("user_id",userId.toString())
                .putMetadata("plan_id",plan.getId().toString())
                ;

        try{
            String stripeCustomerId = user.getStripeCustomerId();

            if(stripeCustomerId !=null && !stripeCustomerId.isBlank())
                params.setCustomer(stripeCustomerId);
            else
                params.setCustomerEmail(user.getUsername());

            Session session = Session.create(params.build()); //api call to Stripe back-end

            return new CheckoutResponse(session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public PortalResponse openCustomerPortal() {
        Long userId = authUtil.getCurrentUserId();
        User user = getUser(userId);
        String stripeCustomerId = user.getStripeCustomerId();

        if(stripeCustomerId==null || stripeCustomerId.isEmpty()){
            throw new BadRequestException("user does not have a Stripe customer id, user id: {}"+userId);
        }

        try {
            com.stripe.model.billingportal.Session portalSession = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(stripeCustomerId)
                            .setReturnUrl(frontendUrl)
                            .build()
            );

            return new PortalResponse(portalSession.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.info("Witnessing stripe event: {}",type);
    /** Subscription event and their definitions
     * customer.subscription.updated - Sent when a subscription starts or changes. For example, renewing a subscription,
     *              adding a coupon, applying a discount, adding an invoice item, and changing plans all trigger this event.
     *
     * customer.subscription.deleted - Sent when a customer’s subscription ends.
     *
     * invoice.paid - Sent when the invoice is successfully paid. You can provision access to your product when you
     *              receive this event and the subscription status is active.
     *
     * invoice.payment_failed - A payment for an invoice failed. The PaymentIntent status changes to requires_action.
     *              The status of the subscription continues to be incomplete only for the subscription’s first invoice.
     *              If a payment fails, you can take several possible actions:
     *                -> Notify the customer.
     *                -> Configure your subscription settings in the Dashboard to enable Smart Retries and other
     *                   revenue recovery features.
     *                -> If you’re using PaymentIntents, collect new payment information and confirm the PaymentIntent.
     *                -> Update the default payment method on the subscription.
     *
     * */

            switch (type){
                case "checkout.session.completed" ->
                        handleCheckoutSessionCompleted((Session) stripeObject, metadata); // one-time, on checkout completed

                case "customer.subscription.updated" ->
                        handleCustomerSubscriptionUpdated((Subscription) stripeObject); // when user cancels, upgrades or any updates

                case "customer.subscription.deleted" ->
                        handleCustomerSubscriptionDeleted((Subscription) stripeObject); // when subscription ends, revoke the access

                case "invoice.paid" ->
                        handleInvoicePaid((Invoice) stripeObject); // when invoice is paid

                case "invoice.payment_failed" ->
                        handleInvoicePaymentFailed((Invoice) stripeObject); // when invoice is not paid, mark as PAST_DUE

                default ->
                    log.debug("Ignoring the event: {}",type);
            }
    }

    public void handleCheckoutSessionCompleted(Session session, Map<String,String> metadata){
        log.info("Handling 'checkout.session.completed' event");
        if(session == null){
            log.error("Session object was null");
            return;
        }
        Long userId = Long.parseLong(metadata.get("user_id"));
        Long planId = Long.parseLong(metadata.get("plan_id"));

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        User user = getUser(userId);
        if(user.getStripeCustomerId() == null){
            user.setStripeCustomerId(customerId);
            userRepo.save(user);
        }

        subscriptionService.activateSubscription(userId,planId,subscriptionId,customerId);
    }

    public void handleCustomerSubscriptionUpdated(Subscription subscription){
        log.info("Handling 'customer.subscription.updated' event");
        if(subscription == null){
            log.error("Subscription object was null inside handleCustomerSubscriptionUpdated");
            return;
        }

        String subscriptionId = subscription.getId();

        SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
        if(status == null){
            log.warn("Unknown status '{}' for subscription {}",subscription.getStatus(),subscriptionId);
        }

        SubscriptionItem item = subscription.getItems().getData().getFirst();
        Instant periodStart = toInstant(item.getCurrentPeriodStart());
        Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

        Long planId = resolvePlanId(item.getPrice());

        subscriptionService.updateSubscription(
                subscriptionId, status, periodStart, periodEnd,
                subscription.getCancelAtPeriodEnd(), planId);

    }

    public void handleCustomerSubscriptionDeleted(Subscription subscription){
        log.info("Handling 'customer.subscription.deleted' event");
        if(subscription == null){
            log.error("Subscription object is null inside handleCustomerSubscriptionDeleted");
            return;
        }

        subscriptionService.cancelSubscription(subscription.getId());
    }

    public void handleInvoicePaid(Invoice invoice){
        log.info("Handling 'invoice.paid' event");
        String subscriptionId = extractSubscriptionIdFromInvoice(invoice);
        if(subscriptionId == null) return;

        try{
            Subscription subscription = Subscription.retrieve(subscriptionId);
            SubscriptionItem item =  subscription.getItems().getData().getFirst();

            Instant periodStart = toInstant(item.getCurrentPeriodStart());
            Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(subscriptionId, periodStart, periodEnd);
        }
        catch (StripeException se){
            throw new RuntimeException(se);
        }
    }

    public void handleInvoicePaymentFailed(Invoice invoice){
        log.info("Handling 'invoice.payment_failed' event");
        String subscriptionId = extractSubscriptionIdFromInvoice(invoice);
        if(subscriptionId == null) return;

        subscriptionService.markSubscriptionPastDue(subscriptionId);
    }

    // /// /// Utility methods
    private User getUser(Long userId) {
        return userRepo.findById(userId).orElseThrow( () ->
                new ResourceNotFoundException("user", userId.toString()));
    }

    private SubscriptionStatus mapStripeStatusToEnum(String status) {

        switch (status) {
            case "active":
                return SubscriptionStatus.ACTIVE;
            case "trialing":
                return SubscriptionStatus.TRIALING;
            case "past_due","unpaid","paused","incomplete_expired":
                return SubscriptionStatus.PAST_DUE;
            case "canceled":
                return SubscriptionStatus.CANCELLED;
            case "incomplete":
                return SubscriptionStatus.INCOMPLETE;
            default:{
                log.warn("Unmapped Stripe status: {}",status);
                return null;
            }
        }
    }

    //convert epoch to instant if not null
    private Instant toInstant(Long epoch){
        return epoch!=null ? Instant.ofEpochSecond(epoch) : null;
    }

///    using Stripe SDK classes

    //resolve planId using the stripe_price_id of stripe Price obj
    private Long resolvePlanId(Price stripePriceObject){
        if (stripePriceObject == null || stripePriceObject.getId() == null) return null;

        return planRepo.findByStripePriceId(stripePriceObject.getId())
                .map(Plan::getId)
                .orElse(null);
    }

    //return subscription_id if present, else null
    private String extractSubscriptionIdFromInvoice(Invoice invoice) {

        Invoice.Parent parent = invoice.getParent();
        if (parent != null) {
            Invoice.Parent.SubscriptionDetails subDetails = parent.getSubscriptionDetails();

            if(subDetails!=null)
                return subDetails.getSubscription();
        }

        return null;
    }

}
