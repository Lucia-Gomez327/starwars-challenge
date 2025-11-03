package com.starwars.domain.port.in;

import com.starwars.domain.model.User;

public interface AuthUseCase {
    User register(String username, String password, String email);
    String login(String username, String password);
    User findByUsername(String username);
    boolean validateToken(String token);
}


