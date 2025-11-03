package com.starwars.infrastructure.adapter.out.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.domain.port.out.SwapiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SwapiClientImpl implements SwapiClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${swapi.base-url}")
    private String baseUrl;
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> fetchAll(String endpoint, Class<T> type) {
        List<T> allResults = new ArrayList<>();
        String url = baseUrl + "/" + endpoint;
        
        try {
            // Intentar obtener sin paginación (caso como films -> "result" array)
            Map<String, Object> flatResponse = restTemplate.getForObject(url, Map.class);
            if (flatResponse != null && flatResponse.containsKey("result")) {
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) flatResponse.get("result");
                for (Map<String, Object> resultMap : resultList) {
                    // Algunas respuestas anidan en "properties" y traen uid/_id a nivel superior
                    Map<String, Object> properties = null;
                    if (resultMap != null && resultMap.containsKey("properties")) {
                        properties = (Map<String, Object>) resultMap.get("properties");
                        if (resultMap.containsKey("uid")) {
                            properties.put("uid", resultMap.get("uid"));
                        }
                        if (resultMap.containsKey("_id")) {
                            properties.put("_id", resultMap.get("_id"));
                        }
                    }
                    T item = convertMapToObject(properties != null ? properties : resultMap, type);
                    if (item != null) {
                        allResults.add(item);
                    }
                }
                log.info("Fetched {} items (flat result) from endpoint: {}", allResults.size(), endpoint);
                return allResults;
            }

            // Si no hubo arreglo plano, intentar flujo paginado estándar ("results" + total_pages)
            Map<String, Object> firstPage = restTemplate.getForObject(url + "?page=1", Map.class);
            if (firstPage != null && firstPage.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) firstPage.get("results");
                for (Map<String, Object> result : results) {
                    T item = convertMapToObject(result, type);
                    if (item != null) {
                        allResults.add(item);
                    }
                }
                Integer totalPages = (Integer) firstPage.get("total_pages");
                if (totalPages != null && totalPages > 1) {
                    for (int page = 2; page <= totalPages && page <= 10; page++) {
                        Map<String, Object> pageResult = restTemplate.getForObject(url + "?page=" + page, Map.class);
                        if (pageResult != null && pageResult.containsKey("results")) {
                            List<Map<String, Object>> pageResults = (List<Map<String, Object>>) pageResult.get("results");
                            for (Map<String, Object> result : pageResults) {
                                T item = convertMapToObject(result, type);
                                if (item != null) {
                                    allResults.add(item);
                                }
                            }
                        }
                    }
                }
            }
            
            log.info("Fetched {} items from endpoint: {}", allResults.size(), endpoint);
        } catch (Exception e) {
            log.error("Error fetching data from SWAPI endpoint: {}", endpoint, e);
        }
        
        return allResults;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T fetchById(String endpoint, String id, Class<T> type) {
        String url = baseUrl + "/" + endpoint + "/" + id;
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("result")) {
                // SWAPI devuelve los datos dentro de "result.properties"
                Map<String, Object> resultMap = (Map<String, Object>) response.get("result");
                if (resultMap != null && resultMap.containsKey("properties")) {
                    Map<String, Object> properties = (Map<String, Object>) resultMap.get("properties");
                    // Agregar el uid del result
                    if (resultMap.containsKey("uid")) {
                        properties.put("uid", resultMap.get("uid"));
                    }
                    if (resultMap.containsKey("_id")) {
                        properties.put("_id", resultMap.get("_id"));
                    }
                    return convertMapToObject(properties, type);
                }
                // Si no hay properties, intentar usar result directamente
                return convertMapToObject(resultMap, type);
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching {} with id {} from SWAPI", endpoint, id, e);
            return null;
        }
    }
    
    @Override
    public <T> SwapiPageResponse<T> fetchPage(String endpoint, int page, int limit, Class<T> type) {
        String url = baseUrl + "/" + endpoint + "?page=" + page + "&limit=" + limit;
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                log.error("Null response from SWAPI for endpoint: {}", endpoint);
                return new SwapiPageResponse<>("error", null, null, null, null, new ArrayList<>());
            }
            
            // Convertir la respuesta a SwapiPageResponse
            SwapiPageResponse<T> pageResponse = new SwapiPageResponse<>();
            pageResponse.setMessage((String) response.get("message"));
            pageResponse.setTotalRecords((Integer) response.get("total_records"));
            pageResponse.setTotalPages((Integer) response.get("total_pages"));
            pageResponse.setPrevious((String) response.get("previous"));
            pageResponse.setNext((String) response.get("next"));
            
            // Convertir los resultados
            List<T> results = new ArrayList<>();
            if (response.containsKey("results")) {
                List<Map<String, Object>> resultsList = (List<Map<String, Object>>) response.get("results");
                for (Map<String, Object> result : resultsList) {
                    T item = convertMapToObject(result, type);
                    if (item != null) {
                        results.add(item);
                    }
                }
            }
            pageResponse.setResults(results);
            
            log.debug("Fetched page {} from SWAPI endpoint: {} - {} results", page, endpoint, results.size());
            return pageResponse;
        } catch (Exception e) {
            log.error("Error fetching page {} from SWAPI endpoint: {}", page, endpoint, e);
            return new SwapiPageResponse<>("error", null, null, null, null, new ArrayList<>());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> fetchByName(String endpoint, String name, Class<T> type) {
        String url = baseUrl + "/" + endpoint + "?name=" + name;
        
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                log.error("Null response from SWAPI for endpoint: {} with name: {}", endpoint, name);
                return new ArrayList<>();
            }
            
            List<T> results = new ArrayList<>();
            
            // Cuando se usa ?name=, SWAPI devuelve "result" (array) en lugar de "results"
            if (response.containsKey("result")) {
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) response.get("result");
                for (Map<String, Object> resultMap : resultList) {
                    // SWAPI devuelve los datos dentro de "properties", similar a fetchById
                    if (resultMap != null && resultMap.containsKey("properties")) {
                        Map<String, Object> properties = (Map<String, Object>) resultMap.get("properties");
                        // Agregar el uid del result
                        if (resultMap.containsKey("uid")) {
                            properties.put("uid", resultMap.get("uid"));
                        }
                        if (resultMap.containsKey("_id")) {
                            properties.put("_id", resultMap.get("_id"));
                        }
                        T item = convertMapToObject(properties, type);
                        if (item != null) {
                            results.add(item);
                        }
                    } else {
                        // Si no hay properties, intentar usar result directamente
                        T item = convertMapToObject(resultMap, type);
                        if (item != null) {
                            results.add(item);
                        }
                    }
                }
            }
            
            log.debug("Fetched {} items from SWAPI endpoint: {} with name: {}", results.size(), endpoint, name);
            return results;
        } catch (Exception e) {
            log.error("Error fetching from SWAPI endpoint: {} with name: {}", endpoint, name, e);
            return new ArrayList<>();
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T convertMapToObject(Map<String, Object> map, Class<T> type) {
        try {
            String json = objectMapper.writeValueAsString(map);
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("Error converting map to object", e);
            return null;
        }
    }
}

