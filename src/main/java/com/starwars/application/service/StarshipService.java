package com.starwars.application.service;

import com.starwars.domain.model.Starship;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.domain.port.in.StarshipUseCase;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiStarshipDTO;
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
public class StarshipService implements StarshipUseCase {
    
    private final SwapiClient swapiClient;
    private final SwapiMapper swapiMapper;
    
    @Override
    public Page<Starship> findAll(Pageable pageable) {
        log.debug("Finding all starships from SWAPI with pageable: {}", pageable);
        
        // SWAPI usa page 1-based y limit, Spring usa page 0-based y size
        int swapiPage = pageable.getPageNumber() + 1;
        int swapiLimit = pageable.getPageSize();
        
        SwapiPageResponse<SwapiStarshipDTO> swapiResponse = swapiClient.fetchPage("starships", swapiPage, swapiLimit, SwapiStarshipDTO.class);
        
        if (swapiResponse == null || swapiResponse.getResults() == null) {
            log.warn("No results from SWAPI for starships");
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        // Convertir DTOs a modelos de dominio
        List<Starship> starshipList = swapiResponse.getResults().stream()
                .map(swapiMapper::toStarship)
                .collect(Collectors.toList());
        
        // Construir Page de Spring desde la respuesta de SWAPI
        long totalElements = swapiResponse.getTotalRecords() != null ? swapiResponse.getTotalRecords() : 0;
        
        return new PageImpl<>(starshipList, pageable, totalElements);
    }

    
    @Override
    public Optional<Starship> findByUid(String uid) {
        log.debug("Finding starship by uid from SWAPI: {}", uid);
        
        try {
            SwapiStarshipDTO dto = swapiClient.fetchById("starships", uid, SwapiStarshipDTO.class);
            if (dto != null) {
                Starship starship = swapiMapper.toStarship(dto);
                return Optional.ofNullable(starship);
            }
        } catch (Exception e) {
            log.error("Error fetching starship by uid from SWAPI: {}", uid, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Page<Starship> findByNameContaining(String name, Pageable pageable) {
        log.debug("Searching starships by name from SWAPI: {}", name);
        
        int swapiPage = 1;
        int swapiLimit = 100;
        
        SwapiPageResponse<SwapiStarshipDTO> swapiResponse = swapiClient.fetchPage("starships", swapiPage, swapiLimit, SwapiStarshipDTO.class);
        
        if (swapiResponse == null || swapiResponse.getResults() == null) {
            log.warn("No results from SWAPI for starships search");
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        String searchName = name.toLowerCase();
        List<Starship> filteredStarships = swapiResponse.getResults().stream()
                .map(swapiMapper::toStarship)
                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredStarships.size());
        
        if (start > filteredStarships.size()) {
            return new PageImpl<>(List.of(), pageable, filteredStarships.size());
        }
        
        List<Starship> paginatedStarships = filteredStarships.subList(start, end);
        
        return new PageImpl<>(paginatedStarships, pageable, filteredStarships.size());
    }

}




