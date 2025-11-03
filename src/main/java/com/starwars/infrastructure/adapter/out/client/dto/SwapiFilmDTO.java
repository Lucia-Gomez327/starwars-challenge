package com.starwars.infrastructure.adapter.out.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapiFilmDTO {
    private String uid;
    private String title;
    
    @JsonProperty("episode_id")
    private Integer episodeId;
    
    @JsonProperty("opening_crawl")
    private String openingCrawl;
    
    private String director;
    private String producer;
    
    @JsonProperty("release_date")
    private LocalDate releaseDate;
    
    private String url;
    
    @JsonProperty("_id")
    private String internalId;
}

