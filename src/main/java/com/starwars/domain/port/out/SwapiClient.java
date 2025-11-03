package com.starwars.domain.port.out;

import com.starwars.domain.model.SwapiPageResponse;

import java.util.List;

public interface SwapiClient {
    <T> List<T> fetchAll(String endpoint, Class<T> type);
    <T> T fetchById(String endpoint, String id, Class<T> type);
    <T> SwapiPageResponse<T> fetchPage(String endpoint, int page, int limit, Class<T> type);
    <T> List<T> fetchByName(String endpoint, String name, Class<T> type);

}




