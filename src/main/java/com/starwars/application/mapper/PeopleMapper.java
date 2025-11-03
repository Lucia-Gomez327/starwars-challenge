package com.starwars.application.mapper;

import com.starwars.application.dto.response.PeopleResponse;
import com.starwars.domain.model.People;
import org.springframework.stereotype.Component;

@Component
public class PeopleMapper {
    
    public PeopleResponse toResponse(People people) {
        return PeopleResponse.builder()
                .id(people.getId())
                .uid(people.getUid())
                .name(people.getName())
                .height(people.getHeight())
                .mass(people.getMass())
                .hairColor(people.getHairColor())
                .skinColor(people.getSkinColor())
                .eyeColor(people.getEyeColor())
                .birthYear(people.getBirthYear())
                .gender(people.getGender())
                .homeworld(people.getHomeworld())
                .build();
    }
}




