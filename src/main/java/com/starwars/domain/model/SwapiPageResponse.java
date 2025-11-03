package com.starwars.domain.model;

import java.util.List;

public class SwapiPageResponse<T> {
    private String message;
    private Integer totalRecords;
    private Integer totalPages;
    private String previous;
    private String next;
    private List<T> results;
    
    public SwapiPageResponse() {
    }
    
    public SwapiPageResponse(String message, Integer totalRecords, Integer totalPages, 
                            String previous, String next, List<T> results) {
        this.message = message;
        this.totalRecords = totalRecords;
        this.totalPages = totalPages;
        this.previous = previous;
        this.next = next;
        this.results = results;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getTotalRecords() {
        return totalRecords;
    }
    
    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public String getPrevious() {
        return previous;
    }
    
    public void setPrevious(String previous) {
        this.previous = previous;
    }
    
    public String getNext() {
        return next;
    }
    
    public void setNext(String next) {
        this.next = next;
    }
    
    public List<T> getResults() {
        return results;
    }
    
    public void setResults(List<T> results) {
        this.results = results;
    }
}

