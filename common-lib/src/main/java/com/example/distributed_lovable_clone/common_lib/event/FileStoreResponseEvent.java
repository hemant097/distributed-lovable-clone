package com.example.distributed_lovable_clone.common_lib.event;

public record FileStoreResponseEvent (
    String sagaId,
    boolean success,
    String errorMessage,
    Long projectId
){}
