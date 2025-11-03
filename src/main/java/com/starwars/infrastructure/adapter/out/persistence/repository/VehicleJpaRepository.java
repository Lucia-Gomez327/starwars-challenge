package com.starwars.infrastructure.adapter.out.persistence.repository;

import com.starwars.infrastructure.adapter.out.persistence.entity.VehicleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, Long> {
    Optional<VehicleEntity> findByUid(String uid);
    Page<VehicleEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByUid(String uid);
}




