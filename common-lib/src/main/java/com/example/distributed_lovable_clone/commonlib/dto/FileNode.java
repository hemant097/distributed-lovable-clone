package com.example.distributed_lovable_clone.commonlib.dto;


public record FileNode(
        String path
) {
    @Override
    public String toString() {
        return path;
    }
}
