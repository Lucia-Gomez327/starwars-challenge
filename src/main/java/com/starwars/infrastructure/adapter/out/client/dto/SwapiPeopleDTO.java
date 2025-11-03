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
public class SwapiPeopleDTO {
    private String uid;
    private String name;
    private String height;
    private String mass;
    
    @JsonProperty("hair_color")
    private String hairColor;
    
    @JsonProperty("skin_color")
    private String skinColor;
    
    @JsonProperty("eye_color")
    private String eyeColor;
    
    @JsonProperty("birth_year")
    private String birthYear;
    
    private String gender;
    private String homeworld;
    private String url;
    
    @JsonProperty("_id")
    private String internalId;
}

