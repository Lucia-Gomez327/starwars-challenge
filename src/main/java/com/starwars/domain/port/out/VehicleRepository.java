package com.starwars.domain.port.out;

import com.starwars.domain.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface VehicleRepository {
    Page<Vehicle> findAll(Pageable pageable);
    Optional<Vehicle> findById(Long id);
    Page<Vehicle> findByNameContaining(String name, Pageable pageable);


}




