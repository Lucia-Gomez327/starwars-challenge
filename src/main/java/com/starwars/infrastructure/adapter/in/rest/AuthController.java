package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.request.LoginRequest;
import com.starwars.application.dto.request.RegisterRequest;
import com.starwars.application.dto.response.AuthResponse;
import com.starwars.domain.model.User;
import com.starwars.domain.port.in.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Authentication endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthUseCase authUseCase;
    
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authUseCase.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail()
        );
        
        String token = authUseCase.login(request.getUsername(), request.getPassword());
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Login with username and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authUseCase.login(request.getUsername(), request.getPassword());
        User user = authUseCase.findByUsername(request.getUsername());
        
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
        
        return ResponseEntity.ok(response);
    }
}




