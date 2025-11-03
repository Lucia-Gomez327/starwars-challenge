package com.starwars.infrastructure.adapter.out.persistence.repository;

import com.starwars.infrastructure.adapter.out.persistence.entity.StarshipEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StarshipJpaRepository extends JpaRepository<StarshipEntity, Long> {
    Optional<StarshipEntity> findByUid(String uid);
    Page<StarshipEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByUid(String uid);
}




