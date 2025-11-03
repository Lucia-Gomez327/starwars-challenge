package com.starwars.domain.port.in;

import com.starwars.domain.model.Starship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StarshipUseCase {
    Page<Starship> findAll(Pageable pageable);
    Optional<Starship> findByUid(String uid);
    Page<Starship> findByNameContaining(String name, Pageable pageable);
}


