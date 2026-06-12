package com.example.distribute_lovable_clone.common_lib.errors;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(resourceName + " not found with identifier: "+resourceId);

    }
}

