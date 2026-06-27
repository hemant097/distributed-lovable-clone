package com.example.distribute_lovable_clone.account_service.controller;

import com.example.distribute_lovable_clone.account_service.dto.subscription.*;
import com.example.distribute_lovable_clone.account_service.service.PaymentProcessor;
import com.example.distribute_lovable_clone.account_service.service.SubscriptionService;
import com.example.distributelovableclone.commonlib.security.AuthUtil;
import com.stripe.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BillingController {
    private final SubscriptionService subscriptionService;
    private final PaymentProcessor paymentProcessor;
    private final AuthUtil authUtil;


    @GetMapping("/api/me/subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription(){
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription());
    }

    @PostMapping("/api/payments/checkout")
    public ResponseEntity<CheckoutResponse> createCheckoutResponse(@RequestBody CheckoutRequest request){
        return ResponseEntity.ok(paymentProcessor.createCheckoutSessionUrl( request));
    }

    @PostMapping("api/payments/portal")
    public ResponseEntity<PortalResponse> openCustomerPortal(){
        return ResponseEntity.ok(paymentProcessor.openCustomerPortal());
    }
}
