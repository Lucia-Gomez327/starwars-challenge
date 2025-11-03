package com.starwars.application.mapper;

import com.starwars.application.dto.response.VehicleResponse;
import com.starwars.domain.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    
    public VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .uid(vehicle.getUid())
                .name(vehicle.getName())
                .model(vehicle.getModel())
                .manufacturer(vehicle.getManufacturer())
                .costInCredits(vehicle.getCostInCredits())
                .length(vehicle.getLength())
                .crew(vehicle.getCrew())
                .passengers(vehicle.getPassengers())
                .cargoCapacity(vehicle.getCargoCapacity())
                .vehicleClass(vehicle.getVehicleClass())
                .build();
    }
}




