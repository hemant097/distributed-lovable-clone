package com.example.distribute_lovable_clone.account_service.entity;

import com.example.distribute_lovable_clone.common_lib.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor @NoArgsConstructor
@Builder
@Entity @Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false,name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false,name = "plan_id")
    Plan plan;

    @Enumerated(EnumType.STRING)
    SubscriptionStatus status;

    String stripeSubscriptionId;

    Instant currentPeriodStart;
    Instant currentPeriodEnd;

    @Builder.Default
    Boolean cancelAtPeriodEnd = false; //default

    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;

}
