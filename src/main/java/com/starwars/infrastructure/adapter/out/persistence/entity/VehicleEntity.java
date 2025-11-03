package com.starwars.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String uid;
    
    @Column(nullable = false)
    private String name;
    
    private String model;
    private String manufacturer;
    
    @Column(name = "cost_in_credits")
    private String costInCredits;
    
    private String length;
    private String crew;
    private String passengers;
    
    @Column(name = "cargo_capacity")
    private String cargoCapacity;
    
    @Column(name = "vehicle_class")
    private String vehicleClass;
    
    private String url;
}




