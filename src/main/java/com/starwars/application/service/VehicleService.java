package com.starwars.application.service;

import com.starwars.domain.model.Vehicle;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.domain.port.in.VehicleUseCase;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiVehicleDTO;
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
public class VehicleService implements VehicleUseCase {
    
    private final SwapiClient swapiClient;
    private final SwapiMapper swapiMapper;
    
    @Override
    public Page<Vehicle> findAll(Pageable pageable) {
        log.debug("Finding all vehicles from SWAPI with pageable: {}", pageable);
        
        int swapiPage = pageable.getPageNumber() + 1;
        int swapiLimit = pageable.getPageSize();
        
        SwapiPageResponse<SwapiVehicleDTO> swapiResponse = swapiClient.fetchPage("vehicles", swapiPage, swapiLimit, SwapiVehicleDTO.class);
        
        if (swapiResponse == null || swapiResponse.getResults() == null) {
            log.warn("No results from SWAPI for vehicles");
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        List<Vehicle> vehicleList = swapiResponse.getResults().stream()
                .map(swapiMapper::toVehicle)
                .collect(Collectors.toList());
        
        long totalElements = swapiResponse.getTotalRecords() != null ? swapiResponse.getTotalRecords() : 0;
        
        return new PageImpl<>(vehicleList, pageable, totalElements);
    }
    
    @Override
    public Optional<Vehicle> findByUid(String uid) {
        log.debug("Finding vehicle by uid from SWAPI: {}", uid);
        
        try {
            SwapiVehicleDTO dto = swapiClient.fetchById("vehicles", uid, SwapiVehicleDTO.class);
            if (dto != null) {
                Vehicle vehicle = swapiMapper.toVehicle(dto);
                return Optional.ofNullable(vehicle);
            }
        } catch (Exception e) {
            log.error("Error fetching vehicle by uid from SWAPI: {}", uid, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Page<Vehicle> findByNameContaining(String name, Pageable pageable) {
        log.debug("Searching vehicles by name from SWAPI: {}", name);
        
        int swapiPage = 1;
        int swapiLimit = 100;
        
        SwapiPageResponse<SwapiVehicleDTO> swapiResponse = swapiClient.fetchPage("vehicles", swapiPage, swapiLimit, SwapiVehicleDTO.class);
        
        if (swapiResponse == null || swapiResponse.getResults() == null) {
            log.warn("No results from SWAPI for vehicles search");
            return new PageImpl<>(List.of(), pageable, 0);
        }
        
        String searchName = name.toLowerCase();
        List<Vehicle> filteredVehicles = swapiResponse.getResults().stream()
                .map(swapiMapper::toVehicle)
                .filter(v -> v.getName() != null && v.getName().toLowerCase().contains(searchName))
                .collect(Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredVehicles.size());
        
        if (start > filteredVehicles.size()) {
            return new PageImpl<>(List.of(), pageable, filteredVehicles.size());
        }
        
        List<Vehicle> paginatedVehicles = filteredVehicles.subList(start, end);
        
        return new PageImpl<>(paginatedVehicles, pageable, filteredVehicles.size());
    }

}




