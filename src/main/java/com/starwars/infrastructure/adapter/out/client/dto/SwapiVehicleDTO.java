package com.starwars.infrastructure.adapter.out.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapiVehicleDTO {
    private String uid;
    private String name;
    private String model;
    private String manufacturer;
    
    @JsonProperty("cost_in_credits")
    private String costInCredits;
    
    private String length;
    private String crew;
    private String passengers;
    
    @JsonProperty("cargo_capacity")
    private String cargoCapacity;
    
    @JsonProperty("vehicle_class")
    private String vehicleClass;
    
    private String url;
    
    @JsonProperty("_id")
    private String internalId;
}

