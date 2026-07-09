package com.example.distributed_lovable_clone.account_service.controller;

import com.example.distributed_lovable_clone.account_service.dto.subscription.CheckoutRequest;
import com.example.distributed_lovable_clone.account_service.dto.subscription.CheckoutResponse;
import com.example.distributed_lovable_clone.account_service.dto.subscription.PortalResponse;
import com.example.distributed_lovable_clone.account_service.dto.subscription.SubscriptionResponse;
import com.example.distributed_lovable_clone.account_service.service.PaymentProcessor;
import com.example.distributed_lovable_clone.account_service.service.SubscriptionService;
import com.example.distributed_lovable_clone.common_lib.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
