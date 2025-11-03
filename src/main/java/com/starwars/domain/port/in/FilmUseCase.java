package com.starwars.domain.port.in;

import com.starwars.domain.model.Film;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FilmUseCase {
    Page<Film> findAll(Pageable pageable);
    Optional<Film> findByUid(String uid);
    Page<Film> findByTitleContaining(String title, Pageable pageable);

}


