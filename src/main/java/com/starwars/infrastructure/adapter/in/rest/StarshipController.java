package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.PageResponse;
import com.starwars.application.dto.response.StarshipResponse;
import com.starwars.application.mapper.StarshipMapper;
import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.Starship;
import com.starwars.domain.port.in.StarshipUseCase;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiPageResponse;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiStarshipDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Starships", description = "Starships management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/starships")
@RequiredArgsConstructor
public class StarshipController {
    
    private final StarshipUseCase starshipUseCase;
    private final StarshipMapper starshipMapper;
    private final SwapiClient swapiClient;
    private final SwapiMapper swapiMapper;
    
    @Operation(summary = "Get all starships, with optional pagination and filters (id/name)")
    @GetMapping
    public ResponseEntity<?> getAllStarships(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // Si hay ID, devolver solo ese registro
        if (id != null && !id.isEmpty()) {
            Starship starship = starshipUseCase.findByUid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Starship", id));
            return ResponseEntity.ok(starshipMapper.toResponse(starship));
        }
        
        // Si hay nombre, buscar por nombre
        if (name != null && !name.isEmpty()) {
            Pageable pageable = (page != null && size != null) 
                    ? PageRequest.of(page, size) 
                    : null;
            return ResponseEntity.ok(searchByName(name, pageable));
        }
        
        // Si hay paginación, devolver paginado
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Starship> starshipPage = starshipUseCase.findAll(pageable);
            return ResponseEntity.ok(PageResponse.<StarshipResponse>builder()
                    .content(starshipPage.getContent().stream()
                            .map(starshipMapper::toResponse)
                            .toList())
                    .pageNumber(starshipPage.getNumber())
                    .pageSize(starshipPage.getSize())
                    .totalElements(starshipPage.getTotalElements())
                    .totalPages(starshipPage.getTotalPages())
                    .last(starshipPage.isLast())
                    .first(starshipPage.isFirst())
                    .build());
        }
        
        // Sin parámetros, devolver todo
        return ResponseEntity.ok(getAll());
    }
    
    private List<StarshipResponse> getAll() {
        List<StarshipResponse> allStarships = new java.util.ArrayList<>();
        int currentPage = 1;
        int limit = 100;
        
        while (true) {
            SwapiPageResponse<SwapiStarshipDTO> swapiResponse = 
                    swapiClient.fetchPage("starships", currentPage, limit, SwapiStarshipDTO.class);
            
            if (swapiResponse == null || swapiResponse.getResults() == null || swapiResponse.getResults().isEmpty()) {
                break;
            }
            
            List<StarshipResponse> starshipPage = swapiResponse.getResults().stream()
                    .map(swapiMapper::toStarship)
                    .map(starshipMapper::toResponse)
                    .collect(Collectors.toList());
            
            allStarships.addAll(starshipPage);
            
            if (swapiResponse.getNext() == null || swapiResponse.getNext().isEmpty()) {
                break;
            }
            
            currentPage++;
        }
        
        return allStarships;
    }
    
    private PageResponse<StarshipResponse> searchByName(String name, Pageable pageable) {
        List<StarshipResponse> allStarships = getAll();
        
        String searchName = name.toLowerCase();
        List<StarshipResponse> filteredStarships = allStarships.stream()
                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
        
        if (pageable == null) {
            return PageResponse.<StarshipResponse>builder()
                    .content(filteredStarships)
                    .totalElements(filteredStarships.size())
                    .build();
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredStarships.size());
        
        if (start > filteredStarships.size()) {
            return PageResponse.<StarshipResponse>builder()
                    .content(List.of())
                    .pageNumber(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredStarships.size())
                    .build();
        }
        
        List<StarshipResponse> paginatedStarships = filteredStarships.subList(start, end);
        
        return PageResponse.<StarshipResponse>builder()
                .content(paginatedStarships)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(filteredStarships.size())
                .totalPages((int) Math.ceil((double) filteredStarships.size() / pageable.getPageSize()))
                .last(end >= filteredStarships.size())
                .first(start == 0)
                .build();
    }
}




