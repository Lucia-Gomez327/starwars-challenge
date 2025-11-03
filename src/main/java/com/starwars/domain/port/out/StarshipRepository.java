package com.starwars.domain.port.out;

import com.starwars.domain.model.Starship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StarshipRepository {
    Page<Starship> findAll(Pageable pageable);
    Optional<Starship> findById(Long id);
    Optional<Starship> findByUid(String uid);
    Page<Starship> findByNameContaining(String name, Pageable pageable);
    Starship save(Starship starship);
    void deleteById(Long id);
    boolean existsByUid(String uid);
}




