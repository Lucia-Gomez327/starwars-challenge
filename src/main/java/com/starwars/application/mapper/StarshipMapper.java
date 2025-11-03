package com.starwars.application.mapper;

import com.starwars.application.dto.response.StarshipResponse;
import com.starwars.domain.model.Starship;
import org.springframework.stereotype.Component;

@Component
public class StarshipMapper {
    
    public StarshipResponse toResponse(Starship starship) {
        return StarshipResponse.builder()
                .id(starship.getId())
                .uid(starship.getUid())
                .name(starship.getName())
                .model(starship.getModel())
                .manufacturer(starship.getManufacturer())
                .costInCredits(starship.getCostInCredits())
                .length(starship.getLength())
                .crew(starship.getCrew())
                .passengers(starship.getPassengers())
                .cargoCapacity(starship.getCargoCapacity())
                .starshipClass(starship.getStarshipClass())
                .build();
    }
}




