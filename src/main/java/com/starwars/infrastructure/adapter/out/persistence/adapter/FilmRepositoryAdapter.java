package com.starwars.infrastructure.adapter.out.persistence.adapter;

import com.starwars.domain.model.Film;
import com.starwars.domain.port.out.FilmRepository;
import com.starwars.infrastructure.adapter.out.persistence.entity.FilmEntity;
import com.starwars.infrastructure.adapter.out.persistence.repository.FilmJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FilmRepositoryAdapter implements FilmRepository {

    private final FilmJpaRepository jpaRepository;

    @Override
    public Page<Film> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public Optional<Film> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }


    // Métodos de conversión privados

    private Film toDomain(FilmEntity entity) {
        return Film.builder()
                .id(entity.getId())
                .uid(entity.getUid())
                .title(entity.getTitle())
                .episodeId(entity.getEpisodeId())
                .openingCrawl(entity.getOpeningCrawl())
                .director(entity.getDirector())
                .producer(entity.getProducer())
                .releaseDate(entity.getReleaseDate())
                .url(entity.getUrl())
                .build();
    }

    private FilmEntity toEntity(Film domain) {
        return FilmEntity.builder()
                .id(domain.getId())
                .uid(domain.getUid())
                .title(domain.getTitle())
                .episodeId(domain.getEpisodeId())
                .openingCrawl(domain.getOpeningCrawl())
                .director(domain.getDirector())
                .producer(domain.getProducer())
                .releaseDate(domain.getReleaseDate())
                .url(domain.getUrl())
                .build();
    }
}

