package com.example.distributelovableclone.commonlib.errors;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(resourceName + " not found with identifier: "+resourceId);

    }
}

