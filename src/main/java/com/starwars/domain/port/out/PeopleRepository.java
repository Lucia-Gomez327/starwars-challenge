package com.starwars.domain.port.out;

import com.starwars.domain.model.People;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PeopleRepository {
    Page<People> findAll(Pageable pageable);
    Optional<People> findById(Long id);

}


