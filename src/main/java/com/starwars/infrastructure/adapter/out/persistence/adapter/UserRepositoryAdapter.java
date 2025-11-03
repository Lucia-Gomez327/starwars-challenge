package com.starwars.infrastructure.adapter.out.persistence.adapter;

import com.starwars.domain.model.User;
import com.starwars.domain.port.out.UserRepository;
import com.starwars.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.starwars.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    // Métodos de conversión privados

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .email(entity.getEmail())
                .roles(convertRolesFromString(entity.getRoles()))
                .createdAt(entity.getCreatedAt())
                .enabled(entity.isEnabled())
                .build();
    }

    private UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .password(domain.getPassword())
                .email(domain.getEmail())
                .roles(convertRolesToString(domain.getRoles()))
                .createdAt(domain.getCreatedAt())
                .enabled(domain.isEnabled())
                .build();
    }

    // Convertir String de roles → Set<String>
    private Set<String> convertRolesFromString(String rolesString) {
        if (rolesString == null || rolesString.isEmpty()) {
            return Set.of("ROLE_USER");
        }
        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    // Convertir Set<String> de roles → String
    private String convertRolesToString(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "ROLE_USER";
        }
        return String.join(",", roles);
    }
}