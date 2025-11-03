package com.starwars.infrastructure.adapter.out.persistence.adapter;

import com.starwars.domain.model.People;
import com.starwars.domain.port.out.PeopleRepository;
import com.starwars.infrastructure.adapter.out.persistence.entity.PeopleEntity;
import com.starwars.infrastructure.adapter.out.persistence.repository.PeopleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PeopleRepositoryAdapter implements PeopleRepository {

    private final PeopleJpaRepository jpaRepository;

    @Override
    public Page<People> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public Optional<People> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private People toDomain(PeopleEntity entity) {
        return People.builder()
                .id(entity.getId())
                .uid(entity.getUid())
                .name(entity.getName())
                .height(entity.getHeight())
                .mass(entity.getMass())
                .hairColor(entity.getHairColor())
                .skinColor(entity.getSkinColor())
                .eyeColor(entity.getEyeColor())
                .birthYear(entity.getBirthYear())
                .gender(entity.getGender())
                .homeworld(entity.getHomeworld())
                .url(entity.getUrl())
                .build();
    }

    private PeopleEntity toEntity(People domain) {
        return PeopleEntity.builder()
                .id(domain.getId())
                .uid(domain.getUid())
                .name(domain.getName())
                .height(domain.getHeight())
                .mass(domain.getMass())
                .hairColor(domain.getHairColor())
                .skinColor(domain.getSkinColor())
                .eyeColor(domain.getEyeColor())
                .birthYear(domain.getBirthYear())
                .gender(domain.getGender())
                .homeworld(domain.getHomeworld())
                .url(domain.getUrl())
                .build();
    }
}