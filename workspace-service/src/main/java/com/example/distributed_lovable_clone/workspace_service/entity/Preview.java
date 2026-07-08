package com.example.distributed_lovable_clone.workspace_service.entity;

import com.example.distributed_lovable_clone.commonlib.enums.PreviewStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Preview {

    Long id;
    Project project;

    String namespace;
    String podName;
    PreviewStatus previewStatus;

    Instant startedAt;
    Instant terminatedAt;
    Instant createdAt;
}
