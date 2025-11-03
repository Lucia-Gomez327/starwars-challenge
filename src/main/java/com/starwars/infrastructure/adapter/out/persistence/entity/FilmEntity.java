package com.starwars.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "films")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilmEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String uid;
    
    @Column(nullable = false)
    private String title;
    
    @Column(name = "episode_id")
    private Integer episodeId;
    
    @Column(name = "opening_crawl", length = 5000)
    private String openingCrawl;
    
    private String director;
    private String producer;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    private String url;
}




