package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.PageResponse;
import com.starwars.application.dto.response.VehicleResponse;
import com.starwars.application.mapper.VehicleMapper;
import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.Vehicle;
import com.starwars.domain.port.in.VehicleUseCase;
 
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiVehicleDTO;
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

@Tag(name = "Vehicles", description = "Vehicles management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    
    private final VehicleUseCase vehicleUseCase;
    private final VehicleMapper vehicleMapper;
    private final SwapiClient swapiClient;
    private final SwapiMapper swapiMapper;
    
    @Operation(summary = "Get vehicles paginated (page is 1-based)")
    @GetMapping
    public ResponseEntity<PageResponse<VehicleResponse>> getAllVehicles(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        int requestedPage = (page == null || page < 1) ? 1 : page;
        int requestedSize = (size == null || size < 1) ? 10 : size;
        Pageable pageable = PageRequest.of(requestedPage - 1, requestedSize);
        Page<Vehicle> vehiclePage = vehicleUseCase.findAll(pageable);
        return ResponseEntity.ok(PageResponse.<VehicleResponse>builder()
                .content(vehiclePage.getContent().stream()
                        .map(vehicleMapper::toResponse)
                        .toList())
                .pageNumber(requestedPage)
                .pageSize(vehiclePage.getSize())
                .totalElements(vehiclePage.getTotalElements())
                .totalPages(vehiclePage.getTotalPages())
                .last(vehiclePage.isLast())
                .first(vehiclePage.isFirst())
                .build());
    }

    @Operation(summary = "Search vehicles by id and/or name or model (not both name and model at the same time, page is 1-based)")
    @GetMapping("/search")
    public ResponseEntity<?> searchVehicles(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        // Si hay ID, devolver solo ese registro
        if (id != null && !id.isEmpty()) {
            Vehicle vehicle = vehicleUseCase.findByUid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
            return ResponseEntity.ok(vehicleMapper.toResponse(vehicle));
        }

        boolean hasName = name != null && !name.isEmpty();
        boolean hasModel = model != null && !model.isEmpty();

        if (hasName && hasModel) {
            return ResponseEntity.badRequest().body("No se puede buscar por 'name' y 'model' al mismo tiempo. Use solo uno.");
        }

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

        return ResponseEntity.badRequest().body("Debe especificar 'id', 'name' o 'model'.");
    }
    
    private PageResponse<VehicleResponse> searchByName(String name, Pageable pageable, Integer requestedPage) {
        List<SwapiVehicleDTO> swapiResults = swapiClient.fetchByName("vehicles", name, SwapiVehicleDTO.class);
        List<VehicleResponse> filteredVehicles = swapiResults.stream()
                .map(swapiMapper::toVehicle)
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());
        
        if (pageable == null) {
            return PageResponse.<VehicleResponse>builder()
                    .content(filteredVehicles)
                    .totalElements(filteredVehicles.size())
                    .build();
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredVehicles.size());
        
        if (start >= filteredVehicles.size()) {
            return PageResponse.<VehicleResponse>builder()
                    .content(List.of())
                    .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredVehicles.size())
                    .build();
        }
        
        List<VehicleResponse> paginatedVehicles = filteredVehicles.subList(start, end);
        
        return PageResponse.<VehicleResponse>builder()
                .content(paginatedVehicles)
                .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                .pageSize(pageable.getPageSize())
                .totalElements(filteredVehicles.size())
                .totalPages((int) Math.ceil((double) filteredVehicles.size() / pageable.getPageSize()))
                .last(end >= filteredVehicles.size())
                .first(start == 0)
                .build();
    }

    private PageResponse<VehicleResponse> searchByModel(String model, Pageable pageable, Integer requestedPage) {
        List<SwapiVehicleDTO> swapiResults = swapiClient.fetchByModel("vehicles", model, SwapiVehicleDTO.class);
        List<VehicleResponse> filteredVehicles = swapiResults.stream()
                .map(swapiMapper::toVehicle)
                .map(vehicleMapper::toResponse)
                .collect(Collectors.toList());

        if (pageable == null) {
            return PageResponse.<VehicleResponse>builder()
                    .content(filteredVehicles)
                    .totalElements(filteredVehicles.size())
                    .build();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredVehicles.size());

        if (start >= filteredVehicles.size()) {
            return PageResponse.<VehicleResponse>builder()
                    .content(List.of())
                    .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredVehicles.size())
                    .build();
        }

        List<VehicleResponse> paginatedVehicles = filteredVehicles.subList(start, end);

        return PageResponse.<VehicleResponse>builder()
                .content(paginatedVehicles)
                .pageNumber(requestedPage != null ? requestedPage : (pageable.getPageNumber() + 1))
                .pageSize(pageable.getPageSize())
                .totalElements(filteredVehicles.size())
                .totalPages((int) Math.ceil((double) filteredVehicles.size() / pageable.getPageSize()))
                .last(end >= filteredVehicles.size())
                .first(start == 0)
                .build();
    }
}




