package com.starwars.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Starship {
    private Long id;
    private String uid;
    private String name;
    private String model;
    private String manufacturer;
    private String costInCredits;
    private String length;
    private String crew;
    private String passengers;
    private String cargoCapacity;
    private String starshipClass;
    private String url;
}