package com.example.distributed_lovable_clone.common_lib.errors;


public class BadRequestException extends RuntimeException{

    public BadRequestException(String message) {
        super(message);
    }

}
