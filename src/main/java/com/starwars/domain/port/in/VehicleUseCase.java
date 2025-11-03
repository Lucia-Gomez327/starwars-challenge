package com.starwars.domain.port.in;

import com.starwars.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface VehicleUseCase {
    Page<Vehicle> findAll(Pageable pageable);
    Optional<Vehicle> findByUid(String uid);
    Page<Vehicle> findByNameContaining(String name, Pageable pageable);
}


