package com.starwars.domain.port.out;

import com.starwars.domain.model.Film;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FilmRepository {
    Page<Film> findAll(Pageable pageable);
    Optional<Film> findById(Long id);
}


