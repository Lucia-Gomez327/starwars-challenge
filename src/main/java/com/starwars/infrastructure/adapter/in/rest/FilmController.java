package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.FilmResponse;
import com.starwars.application.dto.response.PageResponse;
import com.starwars.application.mapper.FilmMapper;
import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.Film;
import com.starwars.domain.port.in.FilmUseCase;
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
    
    @Operation(summary = "Get films paginated (page is 1-based)")
    @GetMapping
    public ResponseEntity<PageResponse<FilmResponse>> getAllFilms(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int requestedPage = (page == null || page < 1) ? 1 : page;
        int requestedSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(requestedPage - 1, requestedSize);
        Page<Film> filmPage = filmUseCase.findAll(pageable);
        return ResponseEntity.ok(PageResponse.<FilmResponse>builder()
                .content(filmPage.getContent().stream()
                        .map(filmMapper::toResponse)
                        .toList())
                .pageNumber(requestedPage)
                .pageSize(filmPage.getSize())
                .totalElements(filmPage.getTotalElements())
                .totalPages(filmPage.getTotalPages())
                .last(filmPage.isLast())
                .first(filmPage.isFirst())
                .build());
    }

    @Operation(summary = "Search films by id and/or title (page is 1-based)")
    @GetMapping("/search")
    public ResponseEntity<?> searchFilms(
            @RequestParam(required = false) String id,
            @RequestParam(required = false, name = "title") String title,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        // Si hay ID, devolver solo ese registro
        if (id != null && !id.isEmpty()) {
            Film film = filmUseCase.findByUid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Film", id));
            return ResponseEntity.ok(filmMapper.toResponse(film));
        }

        // Si hay título, buscar por título
        if (title != null && !title.isEmpty()) {
            Pageable pageable = null;
            Integer requestedPage = null;
            if (page != null && size != null) {
                requestedPage = page;
                int adjusted = Math.max(0, page - 1);
                pageable = PageRequest.of(adjusted, size);
            }
            return ResponseEntity.ok(searchByTitle(title, pageable, requestedPage));
        }

        // Si no hay filtros, devolver 400
        return ResponseEntity.badRequest().body("Debe especificar 'id' o 'title'.");
    }
    
    private List<FilmResponse> getAll() {
        List<SwapiFilmDTO> allDtos = swapiClient.fetchAll("films", SwapiFilmDTO.class);
        return allDtos.stream()
                .map(swapiMapper::toFilm)
                .map(filmMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    private PageResponse<FilmResponse> searchByTitle(String title, Pageable pageable, Integer requestedPage) {
        List<FilmResponse> allFilms = getAll();

        String searchName = title.toLowerCase();
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
                    .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredFilms.size())
                    .build();
        }
        
        List<FilmResponse> paginatedFilms = filteredFilms.subList(start, end);
        
        return PageResponse.<FilmResponse>builder()
                .content(paginatedFilms)
                .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                .pageSize(pageable.getPageSize())
                .totalElements(filteredFilms.size())
                .totalPages((int) Math.ceil((double) filteredFilms.size() / pageable.getPageSize()))
                .last(end >= filteredFilms.size())
                .first(start == 0)
                .build();
    }
}




