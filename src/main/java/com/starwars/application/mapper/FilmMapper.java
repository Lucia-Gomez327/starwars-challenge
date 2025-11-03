package com.starwars.application.mapper;

import com.starwars.application.dto.response.FilmResponse;
import com.starwars.domain.model.Film;
import org.springframework.stereotype.Component;

@Component
public class FilmMapper {
    
    public FilmResponse toResponse(Film film) {
        return FilmResponse.builder()
                .id(film.getId())
                .uid(film.getUid())
                .title(film.getTitle())
                .episodeId(film.getEpisodeId())
                .openingCrawl(film.getOpeningCrawl())
                .director(film.getDirector())
                .producer(film.getProducer())
                .releaseDate(film.getReleaseDate())
                .build();
    }
}




