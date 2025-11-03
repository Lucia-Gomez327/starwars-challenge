package com.starwars.infrastructure.adapter.out.persistence.adapter;

import com.starwars.domain.model.Vehicle;
import com.starwars.domain.port.out.VehicleRepository;
import com.starwars.infrastructure.adapter.out.persistence.entity.VehicleEntity;
import com.starwars.infrastructure.adapter.out.persistence.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VehicleRepositoryAdapter implements VehicleRepository {

    private final VehicleJpaRepository jpaRepository;

    @Override
    public Page<Vehicle> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }


    @Override
    public Page<Vehicle> findByNameContaining(String name, Pageable pageable) {
        return jpaRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toDomain);
    }



    private Vehicle toDomain(VehicleEntity entity) {
        return Vehicle.builder()
                .id(entity.getId())
                .uid(entity.getUid())
                .name(entity.getName())
                .model(entity.getModel())
                .manufacturer(entity.getManufacturer())
                .costInCredits(entity.getCostInCredits())
                .length(entity.getLength())
                .crew(entity.getCrew())
                .passengers(entity.getPassengers())
                .cargoCapacity(entity.getCargoCapacity())
                .vehicleClass(entity.getVehicleClass())
                .url(entity.getUrl())
                .build();
    }

    private VehicleEntity toEntity(Vehicle domain) {
        return VehicleEntity.builder()
                .id(domain.getId())
                .uid(domain.getUid())
                .name(domain.getName())
                .model(domain.getModel())
                .manufacturer(domain.getManufacturer())
                .costInCredits(domain.getCostInCredits())
                .length(domain.getLength())
                .crew(domain.getCrew())
                .passengers(domain.getPassengers())
                .cargoCapacity(domain.getCargoCapacity())
                .vehicleClass(domain.getVehicleClass())
                .url(domain.getUrl())
                .build();
    }
}
