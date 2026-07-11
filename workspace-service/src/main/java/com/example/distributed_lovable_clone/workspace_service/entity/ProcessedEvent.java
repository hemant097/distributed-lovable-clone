package com.example.distributed_lovable_clone.workspace_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
public class ProcessedEvent {

    @Id
    private String sagaId;

    private LocalDateTime processedAt;

}
