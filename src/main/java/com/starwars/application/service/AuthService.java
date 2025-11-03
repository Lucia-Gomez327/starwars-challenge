package com.starwars.application.service;

import com.starwars.domain.exception.AuthenticationException;
import com.starwars.domain.model.User;
import com.starwars.domain.port.in.AuthUseCase;
import com.starwars.domain.port.out.UserRepository;
import com.starwars.infrastructure.adapter.in.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    @Override
    @Transactional
    public User register(String username, String password, String email) {
        log.debug("Registering new user: {}", username);
        
        if (userRepository.existsByUsername(username)) {
            throw new AuthenticationException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("Email already exists");
        }
        
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .roles(Set.of("ROLE_USER"))
                .createdAt(LocalDateTime.now())
                .enabled(true)
                .build();
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public String login(String username, String password) {
        log.debug("User login attempt: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        if (!user.isEnabled()) {
            throw new AuthenticationException("User account is disabled");
        }
        
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}

