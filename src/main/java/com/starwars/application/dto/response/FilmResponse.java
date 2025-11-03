package com.starwars.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmResponse {
    private Long id;
    private String uid;
    private String title;
    private Integer episodeId;
    private String openingCrawl;
    private String director;
    private String producer;
    private LocalDate releaseDate;
}




