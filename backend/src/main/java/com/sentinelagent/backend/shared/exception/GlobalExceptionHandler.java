package com.sentinelagent.backend.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors.toString(),
                LocalDateTime.now());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        String className = ex.getClass().getSimpleName();
        HttpStatus status;
        String error;

        switch (className) {
            case "AgentNotFoundException" -> {
                status = HttpStatus.NOT_FOUND;
                error = "Agent not found";
                log.warn("Agent not found: {}", ex.getMessage());
            }
            case "AgentAlreadyExistsException" -> {
                status = HttpStatus.CONFLICT;
                error = "Agent already registered";
                log.warn("Agent already exists: {}", ex.getMessage());
            }
            case "InvalidAgentCredentialsException" -> {
                status = HttpStatus.UNAUTHORIZED;
                error = "Authentication failed";
                log.warn("Invalid agent credentials: {}", ex.getMessage());
            }
            default -> {
                status = HttpStatus.BAD_REQUEST;
                error = "Domain error";
                log.error("Domain exception: {}", ex.getMessage());
            }
        }

        ErrorResponse response = new ErrorResponse(
                status.value(),
                error,
                ex.getMessage(),
                LocalDateTime.now());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "An unexpected error occurred",
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp) {
    }
}
