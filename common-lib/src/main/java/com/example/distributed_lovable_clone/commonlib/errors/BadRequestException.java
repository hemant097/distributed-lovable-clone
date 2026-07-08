package com.example.distributed_lovable_clone.commonlib.errors;


public class BadRequestException extends RuntimeException{

    public BadRequestException(String message) {
        super(message);
    }

}
