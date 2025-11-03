package com.starwars.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapiPageResponse<T> {
    private String message;
    private Integer totalRecords;
    private Integer totalPages;
    private String previous;
    private String next;
    private List<T> results;
}

