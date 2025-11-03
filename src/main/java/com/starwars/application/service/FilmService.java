package com.starwars.application.service;

import com.starwars.domain.model.Film;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.domain.port.in.FilmUseCase;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiFilmDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService implements FilmUseCase {
    
    private final SwapiClient swapiClient;
    private final SwapiMapper swapiMapper;
    
    @Override
    public Page<Film> findAll(Pageable pageable) {
        log.debug("Finding all films from SWAPI with pageable: {}", pageable);

        // films en swapi.tech no está paginado; traemos todo y paginamos en memoria
        List<SwapiFilmDTO> allDtos = swapiClient.fetchAll("films", SwapiFilmDTO.class);
        List<Film> allFilms = allDtos.stream()
                .map(swapiMapper::toFilm)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allFilms.size());
        if (start >= allFilms.size()) {
            return new PageImpl<>(List.of(), pageable, allFilms.size());
        }
        List<Film> pageContent = allFilms.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allFilms.size());
    }
    
    @Override
    public Optional<Film> findByUid(String uid) {
        log.debug("Finding film by uid from SWAPI: {}", uid);
        
        try {
            SwapiFilmDTO dto = swapiClient.fetchById("films", uid, SwapiFilmDTO.class);
            if (dto != null) {
                Film film = swapiMapper.toFilm(dto);
                return Optional.ofNullable(film);
            }
        } catch (Exception e) {
            log.error("Error fetching film by uid from SWAPI: {}", uid, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Page<Film> findByTitleContaining(String title, Pageable pageable) {
        log.debug("Searching films by title from SWAPI: {}", title);
        
        int swapiPage = 1;
        int swapiLimit = 100;
        
        SwapiPageResponse<SwapiFilmDTO> swapiResponse = swapiClient.fetchPage("films", swapiPage, swapiLimit, SwapiFilmDTO.class);
        
        if (swapiResponse == null || swapiResponse.getResults() == null) {
            log.warn("No results from SWAPI for films search");
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        String searchTitle = title.toLowerCase();
        List<Film> filteredFilms = swapiResponse.getResults().stream()
                .map(swapiMapper::toFilm)
                .filter(f -> f.getTitle() != null && f.getTitle().toLowerCase().contains(searchTitle))
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredFilms.size());
        
        if (start > filteredFilms.size()) {
            return new PageImpl<>(List.of(), pageable, filteredFilms.size());
        }
        
        List<Film> paginatedFilms = filteredFilms.subList(start, end);
        
        return new PageImpl<>(paginatedFilms, pageable, filteredFilms.size());
    }

}




