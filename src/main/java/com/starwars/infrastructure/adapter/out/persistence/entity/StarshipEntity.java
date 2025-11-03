package com.starwars.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "starships")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarshipEntity {
    
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
    
    @Column(name = "starship_class")
    private String starshipClass;
    
    private String url;
}




