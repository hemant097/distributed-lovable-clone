package com.example.distributed_lovable_clone.common_lib.event;

public record FileStoreRequestEvent
        (Long projectId,
         String sagaId,
         String filePath,
         String fileContent,
         Long userId
         )
{}
