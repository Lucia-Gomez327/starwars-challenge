package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.PageResponse;
import com.starwars.application.dto.response.StarshipResponse;
import com.starwars.application.mapper.StarshipMapper;
import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.Starship;
import com.starwars.domain.port.in.StarshipUseCase;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
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
    
    @Operation(summary = "Get starships paginated (page is 1-based, starships supports pagination)")
    @GetMapping
    public ResponseEntity<PageResponse<StarshipResponse>> getAllStarships(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int requestedPage = (page == null || page < 1) ? 1 : page;
        int requestedSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(requestedPage - 1, requestedSize);
        Page<Starship> starshipPage = starshipUseCase.findAll(pageable);
        return ResponseEntity.ok(PageResponse.<StarshipResponse>builder()
                .content(starshipPage.getContent().stream()
                        .map(starshipMapper::toResponse)
                        .toList())
                .pageNumber(requestedPage)
                .pageSize(starshipPage.getSize())
                .totalElements(starshipPage.getTotalElements())
                .totalPages(starshipPage.getTotalPages())
                .last(starshipPage.isLast())
                .first(starshipPage.isFirst())
                .build());
    }

    @Operation(summary = "Search starships by id and/or name or model (not both name and model at the same time, page is 1-based)")
    @GetMapping("/search")
    public ResponseEntity<?> searchStarships(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        // Si hay ID, devolver solo ese registro
        if (id != null && !id.isEmpty()) {
            Starship starship = starshipUseCase.findByUid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Starship", id));
            return ResponseEntity.ok(starshipMapper.toResponse(starship));
        }

        // Validar que no se use name y model a la vez
        boolean hasName = name != null && !name.isEmpty();
        boolean hasModel = model != null && !model.isEmpty();
        
        if (hasName && hasModel) {
            return ResponseEntity.badRequest().body("No se puede buscar por 'name' y 'model' al mismo tiempo. Use solo uno.");
        }

        // Si hay nombre, buscar por nombre
        if (hasName) {
            Pageable pageable = null;
            Integer requestedPage = null;
            if (page != null && size != null) {
                requestedPage = page;
                int adjusted = Math.max(0, page - 1);
                pageable = PageRequest.of(adjusted, size);
            }
            return ResponseEntity.ok(searchByName(name, pageable, requestedPage));
        }

        // Si hay model, buscar por model
        if (hasModel) {
            Pageable pageable = null;
            Integer requestedPage = null;
            if (page != null && size != null) {
                requestedPage = page;
                int adjusted = Math.max(0, page - 1);
                pageable = PageRequest.of(adjusted, size);
            }
            return ResponseEntity.ok(searchByModel(model, pageable, requestedPage));
        }

        // Si no hay filtros, devolver 400
        return ResponseEntity.badRequest().body("Debe especificar 'id', 'name' o 'model'.");
    }
    
    private PageResponse<StarshipResponse> searchByName(String name, Pageable pageable, Integer requestedPage) {
        List<SwapiStarshipDTO> swapiResults = swapiClient.fetchByName("starships", name, SwapiStarshipDTO.class);
        List<StarshipResponse> filteredStarships = swapiResults.stream()
                .map(swapiMapper::toStarship)
                .map(starshipMapper::toResponse)
                .collect(Collectors.toList());
        
        if (pageable == null) {
            return PageResponse.<StarshipResponse>builder()
                    .content(filteredStarships)
                    .totalElements(filteredStarships.size())
                    .build();
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredStarships.size());
        
        if (start >= filteredStarships.size()) {
            return PageResponse.<StarshipResponse>builder()
                    .content(List.of())
                    .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredStarships.size())
                    .build();
        }
        
        List<StarshipResponse> paginatedStarships = filteredStarships.subList(start, end);
        
        return PageResponse.<StarshipResponse>builder()
                .content(paginatedStarships)
                .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                .pageSize(pageable.getPageSize())
                .totalElements(filteredStarships.size())
                .totalPages((int) Math.ceil((double) filteredStarships.size() / pageable.getPageSize()))
                .last(end >= filteredStarships.size())
                .first(start == 0)
                .build();
    }

    private PageResponse<StarshipResponse> searchByModel(String model, Pageable pageable, Integer requestedPage) {
        List<SwapiStarshipDTO> swapiResults = swapiClient.fetchByModel("starships", model, SwapiStarshipDTO.class);
        List<StarshipResponse> filteredStarships = swapiResults.stream()
                .map(swapiMapper::toStarship)
                .map(starshipMapper::toResponse)
                .collect(Collectors.toList());
        
        if (pageable == null) {
            return PageResponse.<StarshipResponse>builder()
                    .content(filteredStarships)
                    .totalElements(filteredStarships.size())
                    .build();
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredStarships.size());
        
        if (start >= filteredStarships.size()) {
            return PageResponse.<StarshipResponse>builder()
                    .content(List.of())
                    .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredStarships.size())
                    .build();
        }
        
        List<StarshipResponse> paginatedStarships = filteredStarships.subList(start, end);
        
        return PageResponse.<StarshipResponse>builder()
                .content(paginatedStarships)
                .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                .pageSize(pageable.getPageSize())
                .totalElements(filteredStarships.size())
                .totalPages((int) Math.ceil((double) filteredStarships.size() / pageable.getPageSize()))
                .last(end >= filteredStarships.size())
                .first(start == 0)
                .build();
    }
}




