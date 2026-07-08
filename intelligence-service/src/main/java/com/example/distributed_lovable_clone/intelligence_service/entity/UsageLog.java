package com.example.distributed_lovable_clone.intelligence_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "usage_logs",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id","date"}) //one log/user/day
})
@Entity
public class UsageLog {

    @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id",nullable = false)
    Long userId;

    @Column(nullable = false)
    LocalDate date;

    Integer tokensUsed;

    String metadata; //JSON of {model_used, prompt_used}

}
