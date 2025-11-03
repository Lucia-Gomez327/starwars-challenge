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
        log.debug("Registering new user: ", username);
        
        if (userRepository.existsByUsername(username)) {
            throw new AuthenticationException("El usario ya existe");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("El email ya está en uso");
        }
        
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .createdAt(LocalDateTime.now())
                .enabled(true)
                .build();
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public String login(String username, String password) {
        log.debug("User login attempt: ", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Credenciales inválidas"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Credenciales inválidas");
        }
        
        if (!user.isEnabled()) {
            throw new AuthenticationException("La cuenta de usuario no está habilitada");
        }
        
        return jwtTokenProvider.generateToken(user.getUsername());
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

