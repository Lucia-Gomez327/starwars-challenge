package com.starwars.application.service;

import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.People;
import com.starwars.domain.port.in.PeopleUseCase;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiPageResponse;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiPeopleDTO;
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
public class PeopleService implements PeopleUseCase {
    
    private final SwapiClient swapiClient;
    private final SwapiMapper swapiMapper;
    
    @Override
    public Page<People> findAll(Pageable pageable) {
        log.debug("Finding all people from SWAPI with pageable: {}", pageable);
        
        // SWAPI usa page 1-based y limit, Spring usa page 0-based y size
        int swapiPage = pageable.getPageNumber() + 1;
        int swapiLimit = pageable.getPageSize();
        
        SwapiPageResponse<SwapiPeopleDTO> swapiResponse = swapiClient.fetchPage("people", swapiPage, swapiLimit, SwapiPeopleDTO.class);
        
        if (swapiResponse == null || swapiResponse.getResults() == null) {
            log.warn("No results from SWAPI for people");
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        // Convertir DTOs a modelos de dominio
        List<People> peopleList = swapiResponse.getResults().stream()
                .map(swapiMapper::toPeople)
                .collect(Collectors.toList());
        
        // Construir Page de Spring desde la respuesta de SWAPI
        long totalElements = swapiResponse.getTotalRecords() != null ? swapiResponse.getTotalRecords() : 0;
        int totalPages = swapiResponse.getTotalPages() != null ? swapiResponse.getTotalPages() : 0;
        
        return new PageImpl<>(peopleList, pageable, totalElements);
    }



    @Override
    public Optional<People> findByUid(String uid) {
        log.debug("Finding people by uid from SWAPI: {}", uid);

        try {
            SwapiPeopleDTO dto = swapiClient.fetchById("people", uid, SwapiPeopleDTO.class);
            if (dto != null) {
                People people = swapiMapper.toPeople(dto);
                return Optional.ofNullable(people);
            }
        } catch (Exception e) {
            log.error("Error fetching people by uid from SWAPI: {}", uid, e);
        }

        return Optional.empty();
    }
    
    @Override
    public Page<People> findByNameContaining(String name, Pageable pageable) {
        log.debug("Searching people by name from SWAPI: {}", name);
        
        // SWAPI no tiene búsqueda por nombre directa, así que buscamos en todas las páginas
        // Por eficiencia, solo buscamos en la primera página y filtramos

        int swapiPage = 1;
        int swapiLimit = 100; // Limite máximo para buscar
        
        SwapiPageResponse<SwapiPeopleDTO> swapiResponse = swapiClient.fetchPage("people", swapiPage, swapiLimit, SwapiPeopleDTO.class);
        
        if (swapiResponse == null || swapiResponse.getResults() == null) {
            log.warn("No results from SWAPI for people search");
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        // Filtrar por nombre (búsqueda parcial case-insensitive)
        String searchName = name.toLowerCase();
        List<People> filteredPeople = swapiResponse.getResults().stream()
                .map(swapiMapper::toPeople)
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
        
        // Aplicar paginación manual a los resultados filtrados
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredPeople.size());
        
        if (start > filteredPeople.size()) {
            return new PageImpl<>(List.of(), pageable, filteredPeople.size());
        }
        
        List<People> paginatedPeople = filteredPeople.subList(start, end);
        
        return new PageImpl<>(paginatedPeople, pageable, filteredPeople.size());
    }


}




