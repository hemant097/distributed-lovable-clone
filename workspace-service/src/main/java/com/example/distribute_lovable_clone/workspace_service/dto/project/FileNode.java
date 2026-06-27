package com.example.distribute_lovable_clone.workspace_service.dto.project;

import org.jetbrains.annotations.NotNull;

public record FileNode(
        String path
) {
    @NotNull
    @Override
    public String toString() {
        return path;
    }
}
