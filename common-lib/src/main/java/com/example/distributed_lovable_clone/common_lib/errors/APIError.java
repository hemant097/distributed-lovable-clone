package com.example.distributed_lovable_clone.common_lib.errors;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record APIError(
                        HttpStatus httpStatus,
                        String message,
                        Instant timeStamp,
                        @JsonInclude(JsonInclude.Include.NON_NULL)
                        List<APIFieldError> subErrors
) {

        public APIError(HttpStatus httpStatus, String message){
          this(httpStatus, message, Instant.now(),null);
        }

        public APIError(HttpStatus httpStatus, String message, List<APIFieldError> subErrors){
            this(httpStatus, message, Instant.now(), subErrors);
        }
}

record APIFieldError(String field, String message){

}

