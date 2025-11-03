package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.FilmResponse;
import com.starwars.application.dto.response.PageResponse;
import com.starwars.application.mapper.FilmMapper;
import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.Film;
import com.starwars.domain.port.in.FilmUseCase;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiFilmDTO;
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

@Tag(name = "Films", description = "Films management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/films")
@RequiredArgsConstructor
public class FilmController {
    
    private final FilmUseCase filmUseCase;
    private final FilmMapper filmMapper;
    private final SwapiClient swapiClient;
    private final SwapiMapper swapiMapper;
    
    @Operation(summary = "Get all films, with optional pagination and filters (id/name)")
    @GetMapping
    public ResponseEntity<?> getAllFilms(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // Si hay ID, devolver solo ese registro
        if (id != null && !id.isEmpty()) {
            Film film = filmUseCase.findByUid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Film", id));
            return ResponseEntity.ok(filmMapper.toResponse(film));
        }
        
        // Si hay nombre, buscar por título
        if (name != null && !name.isEmpty()) {
            Pageable pageable = (page != null && size != null) 
                    ? PageRequest.of(page, size) 
                    : null;
            return ResponseEntity.ok(searchByName(name, pageable));
        }
        
        // Si hay paginación, devolver paginado
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Film> filmPage = filmUseCase.findAll(pageable);
            return ResponseEntity.ok(PageResponse.<FilmResponse>builder()
                    .content(filmPage.getContent().stream()
                            .map(filmMapper::toResponse)
                            .toList())
                    .pageNumber(filmPage.getNumber())
                    .pageSize(filmPage.getSize())
                    .totalElements(filmPage.getTotalElements())
                    .totalPages(filmPage.getTotalPages())
                    .last(filmPage.isLast())
                    .first(filmPage.isFirst())
                    .build());
        }
        
        // Sin parámetros, devolver todo
        return ResponseEntity.ok(getAll());
    }
    
    private List<FilmResponse> getAll() {
        List<FilmResponse> allFilms = new java.util.ArrayList<>();
        int currentPage = 1;
        int limit = 100;
        
        while (true) {
            SwapiPageResponse<SwapiFilmDTO> swapiResponse = 
                    swapiClient.fetchPage("films", currentPage, limit, SwapiFilmDTO.class);
            
            if (swapiResponse == null || swapiResponse.getResults() == null || swapiResponse.getResults().isEmpty()) {
                break;
            }
            
            List<FilmResponse> filmPage = swapiResponse.getResults().stream()
                    .map(swapiMapper::toFilm)
                    .map(filmMapper::toResponse)
                    .collect(Collectors.toList());
            
            allFilms.addAll(filmPage);
            
            if (swapiResponse.getNext() == null || swapiResponse.getNext().isEmpty()) {
                break;
            }
            
            currentPage++;
        }
        
        return allFilms;
    }
    
    private PageResponse<FilmResponse> searchByName(String name, Pageable pageable) {
        List<FilmResponse> allFilms = getAll();
        
        String searchName = name.toLowerCase();
        List<FilmResponse> filteredFilms = allFilms.stream()
                .filter(f -> f.getTitle() != null && f.getTitle().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
        
        if (pageable == null) {
            return PageResponse.<FilmResponse>builder()
                    .content(filteredFilms)
                    .totalElements(filteredFilms.size())
                    .build();
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredFilms.size());
        
        if (start > filteredFilms.size()) {
            return PageResponse.<FilmResponse>builder()
                    .content(List.of())
                    .pageNumber(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredFilms.size())
                    .build();
        }
        
        List<FilmResponse> paginatedFilms = filteredFilms.subList(start, end);
        
        return PageResponse.<FilmResponse>builder()
                .content(paginatedFilms)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(filteredFilms.size())
                .totalPages((int) Math.ceil((double) filteredFilms.size() / pageable.getPageSize()))
                .last(end >= filteredFilms.size())
                .first(start == 0)
                .build();
    }
}




