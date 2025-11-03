package com.starwars.infrastructure.adapter.out.persistence.adapter;

import com.starwars.domain.model.Starship;
import com.starwars.domain.port.out.StarshipRepository;
import com.starwars.infrastructure.adapter.out.persistence.entity.StarshipEntity;
import com.starwars.infrastructure.adapter.out.persistence.repository.StarshipJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StarshipRepositoryAdapter implements StarshipRepository {

    private final StarshipJpaRepository jpaRepository;

    @Override
    public Page<Starship> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public Optional<Starship> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Starship> findByUid(String uid) {
        return jpaRepository.findByUid(uid).map(this::toDomain);
    }

    @Override
    public Page<Starship> findByNameContaining(String name, Pageable pageable) {
        return jpaRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toDomain);
    }

    @Override
    public Starship save(Starship starship) {
        StarshipEntity entity = toEntity(starship);
        StarshipEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUid(String uid) {
        return jpaRepository.existsByUid(uid);
    }

    // Métodos de conversión privados

    private Starship toDomain(StarshipEntity entity) {
        return Starship.builder()
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
                .starshipClass(entity.getStarshipClass())
                .url(entity.getUrl())
                .build();
    }

    private StarshipEntity toEntity(Starship domain) {
        return StarshipEntity.builder()
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
                .starshipClass(domain.getStarshipClass())
                .url(domain.getUrl())
                .build();
    }
}

