package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.PageResponse;
import com.starwars.application.dto.response.PeopleResponse;
import com.starwars.application.dto.response.StandardResponse;
import com.starwars.application.mapper.PeopleMapper;
import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.People;
import com.starwars.domain.port.in.PeopleUseCase;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiPeopleDTO;
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
    
    @Operation(summary = "Get all people with pagination")
    @GetMapping
    public ResponseEntity<StandardResponse<PageResponse<PeopleResponse>>> getAllPeople(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<People> peoplePage = peopleUseCase.findAll(pageable);
        
        PageResponse<PeopleResponse> pageData = PageResponse.<PeopleResponse>builder()
                .content(peoplePage.getContent().stream()
                        .map(peopleMapper::toResponse)
                        .toList())
                .pageNumber(peoplePage.getNumber())
                .pageSize(peoplePage.getSize())
                .totalElements(peoplePage.getTotalElements())
                .totalPages(peoplePage.getTotalPages())
                .last(peoplePage.isLast())
                .first(peoplePage.isFirst())
                .build();
        
        StandardResponse<PageResponse<PeopleResponse>> response = StandardResponse.exito(pageData);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Search people by id and/or name")
    @GetMapping("/search")
    public ResponseEntity<StandardResponse<?>> searchPeople(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name) {
        
        // Si hay ID, devolver solo ese registro
        if (id != null && !id.isEmpty()) {
            People people = peopleUseCase.findByUid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("People", id));
            StandardResponse<PeopleResponse> response = StandardResponse.exito(peopleMapper.toResponse(people));
            return ResponseEntity.ok(response);
        }
        
        // Si hay nombre, buscar por nombre
        if (name != null && !name.isEmpty()) {
            List<SwapiPeopleDTO> swapiResults = swapiClient.fetchByName("people", name, SwapiPeopleDTO.class);
            
            List<PeopleResponse> filteredPeople = swapiResults.stream()
                    .map(swapiMapper::toPeople)
                    .map(peopleMapper::toResponse)
                    .collect(Collectors.toList());
            
            StandardResponse<List<PeopleResponse>> response = StandardResponse.exito(filteredPeople);
            return ResponseEntity.ok(response);
        }
        
        // Si no hay parámetros, devolver error
        StandardResponse<?> response = StandardResponse.error("Debe proporcionar al menos un parámetro de búsqueda (id o nombre)");
        return ResponseEntity.badRequest().body(response);
    }
    
}




