package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.StandardResponse;
import com.starwars.domain.exception.AuthenticationException;
import com.starwars.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        StandardResponse<?> response = StandardResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        StandardResponse<?> response = StandardResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        StandardResponse<Map<String, String>> response = StandardResponse.error("error tanto", 
                "Errores de validación");
        response.setDatos(errors);
        return ResponseEntity.badRequest().body(response);
    }
}




