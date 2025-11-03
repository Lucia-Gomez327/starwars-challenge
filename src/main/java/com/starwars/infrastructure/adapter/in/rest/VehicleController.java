package com.starwars.infrastructure.adapter.in.rest;

import com.starwars.application.dto.response.PageResponse;
import com.starwars.application.dto.response.VehicleResponse;
import com.starwars.application.mapper.VehicleMapper;
import com.starwars.domain.exception.ResourceNotFoundException;
import com.starwars.domain.model.Vehicle;
import com.starwars.domain.port.in.VehicleUseCase;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiPageResponse;
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
    
    @Operation(summary = "Get all vehicles, with optional pagination and filters (id/name)")
    @GetMapping
    public ResponseEntity<?> getAllVehicles(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // Si hay ID, devolver solo ese registro
        if (id != null && !id.isEmpty()) {
            Vehicle vehicle = vehicleUseCase.findByUid(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
            return ResponseEntity.ok(vehicleMapper.toResponse(vehicle));
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
            Page<Vehicle> vehiclePage = vehicleUseCase.findAll(pageable);
            return ResponseEntity.ok(PageResponse.<VehicleResponse>builder()
                    .content(vehiclePage.getContent().stream()
                            .map(vehicleMapper::toResponse)
                            .toList())
                    .pageNumber(vehiclePage.getNumber())
                    .pageSize(vehiclePage.getSize())
                    .totalElements(vehiclePage.getTotalElements())
                    .totalPages(vehiclePage.getTotalPages())
                    .last(vehiclePage.isLast())
                    .first(vehiclePage.isFirst())
                    .build());
        }
        
        // Sin parámetros, devolver todo
        return ResponseEntity.ok(getAll());
    }
    
    private List<VehicleResponse> getAll() {
        List<VehicleResponse> allVehicles = new java.util.ArrayList<>();
        int currentPage = 1;
        int limit = 100;
        
        while (true) {
            SwapiPageResponse<SwapiVehicleDTO> swapiResponse = 
                    swapiClient.fetchPage("vehicles", currentPage, limit, SwapiVehicleDTO.class);
            
            if (swapiResponse == null || swapiResponse.getResults() == null || swapiResponse.getResults().isEmpty()) {
                break;
            }
            
            List<VehicleResponse> vehiclePage = swapiResponse.getResults().stream()
                    .map(swapiMapper::toVehicle)
                    .map(vehicleMapper::toResponse)
                    .collect(Collectors.toList());
            
            allVehicles.addAll(vehiclePage);
            
            if (swapiResponse.getNext() == null || swapiResponse.getNext().isEmpty()) {
                break;
            }
            
            currentPage++;
        }
        
        return allVehicles;
    }
    
    private PageResponse<VehicleResponse> searchByName(String name, Pageable pageable) {
        List<VehicleResponse> allVehicles = getAll();
        
        String searchName = name.toLowerCase();
        List<VehicleResponse> filteredVehicles = allVehicles.stream()
                .filter(v -> v.getName() != null && v.getName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
        
        if (pageable == null) {
            return PageResponse.<VehicleResponse>builder()
                    .content(filteredVehicles)
                    .totalElements(filteredVehicles.size())
                    .build();
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredVehicles.size());
        
        if (start > filteredVehicles.size()) {
            return PageResponse.<VehicleResponse>builder()
                    .content(List.of())
                    .pageNumber(pageable.getPageNumber())
                    .pageSize(pageable.getPageSize())
                    .totalElements(filteredVehicles.size())
                    .build();
        }
        
        List<VehicleResponse> paginatedVehicles = filteredVehicles.subList(start, end);
        
        return PageResponse.<VehicleResponse>builder()
                .content(paginatedVehicles)
                .pageNumber(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(filteredVehicles.size())
                .totalPages((int) Math.ceil((double) filteredVehicles.size() / pageable.getPageSize()))
                .last(end >= filteredVehicles.size())
                .first(start == 0)
                .build();
    }
}




