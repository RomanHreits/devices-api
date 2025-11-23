package com.example.devicesapi.controller;

import com.example.devicesapi.exception.BlockedResourceException;
import com.example.devicesapi.exception.DuplicatedDataException;
import com.example.devicesapi.exception.InvalidInputPropertyException;
import com.example.devicesapi.exception.ResourceNotFoundException;
import com.example.devicesapi.model.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorDetails = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(400)
                .body(new ErrorResponse("Validation failed", errorDetails));
    }

    @ExceptionHandler(InvalidInputPropertyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPropertyException(InvalidInputPropertyException ex) {
        String errorDetails = ex.getMessage();
        return ResponseEntity.status(400)
                .body(new ErrorResponse("Validation failed", errorDetails));
    }

    @ExceptionHandler(DuplicatedDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(DuplicatedDataException ex) {
        return ResponseEntity.status(409)
                .body(new ErrorResponse("Database constraint violation", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ErrorResponse("Resource not found", ex.getMessage()));
    }

    @ExceptionHandler(BlockedResourceException.class)
    public ResponseEntity<ErrorResponse> handleBlockedResourceException(BlockedResourceException ex) {
        return ResponseEntity.status(423)
                .body(new ErrorResponse("Resource is blocked", ex.getMessage()));
    }
}
