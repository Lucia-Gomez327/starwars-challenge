package com.starwars.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Long id;
    private String uid;
    private String title;
    private Integer episodeId;
    private String openingCrawl;
    private String director;
    private String producer;
    private LocalDate releaseDate;
    private String url;
}


