package com.example.distributed_lovable_clone.account_service.controller;

import com.example.distributed_lovable_clone.account_service.service.PaymentProcessor;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks")
@Slf4j
public class WebhookController {

    //after we do this in stripe cli - stripe listen --forward-to localhost:8080/api/v1/webhook/payment
    //Stripe CLI listens for the event it received from Stripe server and forwards to our localhost app
    //This endpoint waits for payment updates, In the post request, there is payload, signature. We will verify using this
    // signature, that only Stripe is calling this endpoint and no one else.

    private final PaymentProcessor paymentProcessor;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/payment")
    public ResponseEntity<Void> handlePaymentWebhooks(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String signatureHeader){
        try{
            Event event = Webhook.constructEvent(payload, signatureHeader, endpointSecret);

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;

            if(deserializer.getObject().isPresent())
                stripeObject = deserializer.getObject().get();
            else {
                try {
                    stripeObject = deserializer.deserializeUnsafe();
                    if (stripeObject == null) {
                        log.warn("Failed to deserialize webhook object for event: {}", event.getType());
                        return ResponseEntity.ok().build();
                    }
                } catch (Exception ex) {
                    log.error("Unsafe deserialization failed for event: {}: {}", event.getType(), ex.getMessage());
                }
            }

            Map<String,String> metadata = new HashMap<>();

            if(stripeObject instanceof  Session session)
                metadata = session.getMetadata();


            paymentProcessor.handleWebhookEvent(event.getType(), stripeObject, metadata);
            return ResponseEntity.ok().build();
        }catch (SignatureVerificationException e){
            throw new RuntimeException(e);
        }
    }
}

