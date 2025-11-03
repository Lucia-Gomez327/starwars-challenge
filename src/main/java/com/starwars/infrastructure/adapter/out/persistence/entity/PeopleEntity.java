package com.starwars.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "people")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeopleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String uid;
    
    @Column(nullable = false)
    private String name;
    
    private String height;
    private String mass;
    
    @Column(name = "hair_color")
    private String hairColor;
    
    @Column(name = "skin_color")
    private String skinColor;
    
    @Column(name = "eye_color")
    private String eyeColor;
    
    @Column(name = "birth_year")
    private String birthYear;
    
    private String gender;
    private String homeworld;
    private String url;
}




