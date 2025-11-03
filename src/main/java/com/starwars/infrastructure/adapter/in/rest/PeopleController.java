package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.PageResponse;
import com.starwars.application.dto.response.PeopleResponse;
import com.starwars.application.mapper.PeopleMapper;
import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.People;
import com.starwars.domain.port.in.PeopleUseCase;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiPeopleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "People", description = "People management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/people")
@RequiredArgsConstructor
public class PeopleController {
    
    private final PeopleUseCase peopleUseCase;
    private final PeopleMapper peopleMapper;
    private final SwapiClient swapiClient;
    private final SwapiMapper swapiMapper;
    
    @Operation(summary = "Get all people, with optional pagination and filters (id/name)")
    @GetMapping
    public ResponseEntity<?> getAllPeople(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // Si hay ID, devolver solo ese registro
        if (id != null && !id.isEmpty()) {
            People people = peopleUseCase.findByUid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("People", id));
            return ResponseEntity.ok(peopleMapper.toResponse(people));
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
            Page<People> peoplePage = peopleUseCase.findAll(pageable);
            return ResponseEntity.ok(PageResponse.<PeopleResponse>builder()
                    .content(peoplePage.getContent().stream()
                            .map(peopleMapper::toResponse)
                            .toList())
                    .pageNumber(peoplePage.getNumber())
                    .pageSize(peoplePage.getSize())
                    .totalElements(peoplePage.getTotalElements())
                    .totalPages(peoplePage.getTotalPages())
                    .last(peoplePage.isLast())
                    .first(peoplePage.isFirst())
                    .build());
        }
        
        // Sin parámetros, devolver todo
        return ResponseEntity.ok(getAll());
    }
    
    private List<PeopleResponse> getAll() {
        // Obtener todas las páginas de SWAPI
        List<PeopleResponse> allPeople = new java.util.ArrayList<>();
        int currentPage = 1;
        int limit = 100;
        
        while (true) {
            SwapiPageResponse<SwapiPeopleDTO> swapiResponse = 
                    swapiClient.fetchPage("people", currentPage, limit, SwapiPeopleDTO.class);
            
            if (swapiResponse == null || swapiResponse.getResults() == null || swapiResponse.getResults().isEmpty()) {
                break;
            }
            
            List<PeopleResponse> peoplePage = swapiResponse.getResults().stream()
                    .map(swapiMapper::toPeople)
                    .map(peopleMapper::toResponse)
                    .collect(Collectors.toList());
            
            allPeople.addAll(peoplePage);
            
            // Si no hay más páginas, salir
            if (swapiResponse.getNext() == null || swapiResponse.getNext().isEmpty()) {
                break;
            }
            
            currentPage++;
        }
        
        return allPeople;
    }
    
    private PageResponse<PeopleResponse> searchByName(String name, Pageable pageable) {
        List<PeopleResponse> allPeople = getAll();
        
        // Filtrar por nombre
        String searchName = name.toLowerCase();
        List<PeopleResponse> filteredPeople = allPeople.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
        
        // Si no hay paginación, devolver todos
        if (pageable == null) {
            return PageResponse.<PeopleResponse>builder()
                    .content(filteredPeople)
                    .totalElements(filteredPeople.size())
                    .build();
        }
        
        // Aplicar paginación
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredPeople.size());
        
        if (start > filteredPeople.size()) {
            return PageResponse.<PeopleResponse>builder()
                    .content(List.of())
                    .pageNumber(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredPeople.size())
                    .build();
        }
        
        List<PeopleResponse> paginatedPeople = filteredPeople.subList(start, end);
        
        return PageResponse.<PeopleResponse>builder()
                .content(paginatedPeople)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(filteredPeople.size())
                .totalPages((int) Math.ceil((double) filteredPeople.size() / pageable.getPageSize()))
                .last(end >= filteredPeople.size())
                .first(start == 0)
                .build();
    }
}




