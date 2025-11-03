package com.starwars.infrastructure.adapter.out.client;

import com.starwars.domain.model.*;
import com.starwars.infrastructure.adapter.out.client.dto.*;
import org.springframework.stereotype.Component;

@Component
public class SwapiMapper {
    
    public People toPeople(SwapiPeopleDTO dto) {
        if (dto == null) return null;
        
        return People.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .height(dto.getHeight())
                .mass(dto.getMass())
                .hairColor(dto.getHairColor())
                .skinColor(dto.getSkinColor())
                .eyeColor(dto.getEyeColor())
                .birthYear(dto.getBirthYear())
                .gender(dto.getGender())
                .homeworld(dto.getHomeworld())
                .url(dto.getUrl())
                .build();
    }
    
    public Film toFilm(SwapiFilmDTO dto) {
        if (dto == null) return null;
        
        return Film.builder()
                .uid(dto.getUid())
                .title(dto.getTitle())
                .episodeId(dto.getEpisodeId())
                .openingCrawl(dto.getOpeningCrawl())
                .director(dto.getDirector())
                .producer(dto.getProducer())
                .releaseDate(dto.getReleaseDate())
                .url(dto.getUrl())
                .build();
    }
    
    public Starship toStarship(SwapiStarshipDTO dto) {
        if (dto == null) return null;
        
        return Starship.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .model(dto.getModel())
                .manufacturer(dto.getManufacturer())
                .costInCredits(dto.getCostInCredits())
                .length(dto.getLength())
                .crew(dto.getCrew())
                .passengers(dto.getPassengers())
                .cargoCapacity(dto.getCargoCapacity())
                .starshipClass(dto.getStarshipClass())
                .url(dto.getUrl())
                .build();
    }
    
    public Vehicle toVehicle(SwapiVehicleDTO dto) {
        if (dto == null) return null;
        
        return Vehicle.builder()
                .uid(dto.getUid())
                .name(dto.getName())
                .model(dto.getModel())
                .manufacturer(dto.getManufacturer())
                .costInCredits(dto.getCostInCredits())
                .length(dto.getLength())
                .crew(dto.getCrew())
                .passengers(dto.getPassengers())
                .cargoCapacity(dto.getCargoCapacity())
                .vehicleClass(dto.getVehicleClass())
                .url(dto.getUrl())
                .build();
    }
}

