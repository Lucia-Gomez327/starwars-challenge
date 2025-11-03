package com.starwars.infrastructure.adapter.out.persistence.repository;

import com.starwars.infrastructure.adapter.out.persistence.entity.PeopleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeopleJpaRepository extends JpaRepository<PeopleEntity, Long> {
    Optional<PeopleEntity> findByUid(String uid);
    Page<PeopleEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByUid(String uid);
}


