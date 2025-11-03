package com.starwars.domain.port.in;

import com.starwars.domain.model.People;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PeopleUseCase {
    Page<People> findAll(Pageable pageable);
    Optional<People> findByUid(String uid);
    Page<People> findByNameContaining(String name, Pageable pageable);
}


