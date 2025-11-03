package com.starwars.infrastructure.adapter.out.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("total_records")
    private Integer totalRecords;
    
    @JsonProperty("total_pages")
    private Integer totalPages;
    
    private String previous;
    private String next;
    
    private List<T> results;
}

