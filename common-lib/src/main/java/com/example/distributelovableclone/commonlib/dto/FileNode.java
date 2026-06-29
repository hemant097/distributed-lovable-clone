package com.example.distributelovableclone.commonlib.dto;


public record FileNode(
        String path
) {
    @Override
    public String toString() {
        return path;
    }
}
