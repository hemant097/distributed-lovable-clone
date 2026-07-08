package com.example.distributed_lovable_clone.common_lib.errors;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIError> handleResourceNotFound(ResourceNotFoundException resourceNotFoundException) {

        APIError apiError = new APIError(HttpStatus.NOT_FOUND,
                resourceNotFoundException.getMessage());

        log.error("ResourceNotFoundException thrown : {}", apiError.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIError> handleInputValidationError(MethodArgumentNotValidException manve) {

        //getting all the binding errors from the exception and converting into List
        List<APIFieldError> errorList = manve.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error-> new APIFieldError(error.getField(), error.getDefaultMessage()) )
                .toList(); //returns an unmodifiable list

        APIError apiError = new APIError(HttpStatus.BAD_REQUEST, "Input validation failed", errorList);

        log.error("MethodArgumentNotValidException thrown : {}", apiError.message());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<APIError> handleBadRequest(BadRequestException badRequestException) {

        APIError apiError = new APIError(HttpStatus.BAD_REQUEST,
                badRequestException.getMessage());

        log.error("BadRequestException thrown : {}", apiError.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);

    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<APIError> handleUsernameNotFound(UsernameNotFoundException unfe) {

        APIError apiError = new APIError(HttpStatus.NOT_FOUND, unfe.getMessage());

        log.error("UsernameNotFoundException thrown : {}", apiError.message());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<APIError> handleAuthenticationException(AuthenticationException authEx) {

        APIError apiError = new APIError(HttpStatus.UNAUTHORIZED, authEx.getMessage());

        log.error("AuthenticationException thrown : {}", apiError.message());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<APIError> handleJwtException(JwtException exception) {

        APIError apiError = new APIError(HttpStatus.UNAUTHORIZED, exception.getMessage());

        log.error("JwtException thrown : {}", apiError.message());
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIError> handleAccessDeniedException(AccessDeniedException exception) {


        APIError apiError = new APIError(HttpStatus.FORBIDDEN,
                exception.getMessage());

        log.error("AccessDeniedException thrown : {}", apiError.message());
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIError> internalServerError(Exception exception) {

        APIError apiError = new APIError(HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage());

        log.error("Some Exception occured : {}", apiError.message());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}