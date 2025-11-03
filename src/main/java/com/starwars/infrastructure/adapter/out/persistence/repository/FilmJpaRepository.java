package com.starwars.infrastructure.adapter.out.persistence.repository;

import com.starwars.infrastructure.adapter.out.persistence.entity.FilmEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilmJpaRepository extends JpaRepository<FilmEntity, Long> {
    Optional<FilmEntity> findByUid(String uid);
    Page<FilmEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    boolean existsByUid(String uid);
}




