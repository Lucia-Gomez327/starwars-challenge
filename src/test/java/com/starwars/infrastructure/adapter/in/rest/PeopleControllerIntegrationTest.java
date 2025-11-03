package com.starwars.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.application.dto.request.RegisterRequest;
import com.starwars.application.dto.response.AuthResponse;

/**
 * Tests de integración para PeopleController.
 * 
 * A diferencia de los tests unitarios que usan mocks, estos tests cargan
 * toda la aplicación Spring Boot y hacen peticiones reales a SWAPI.
 * Esto nos permite verificar que todo el flujo funciona correctamente
 * desde el controlador hasta el cliente SWAPI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PeopleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    /**
     * Antes de cada test, creamos un usuario de prueba y obtenemos
     * un token JWT válido para usar en las peticiones autenticadas.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Primero registramos un usuario nuevo
        RegisterRequest registerRequest = new RegisterRequest(
                "testuser",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Luego hacemos login para obtener el token JWT
        String loginJson = """
                {
                    "username": "testuser",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        // Extraemos el token de la respuesta
        String responseBody = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        jwtToken = authResponse.getToken();

        // Verificamos que obtuvimos un token válido
        assertThat(jwtToken).isNotNull().isNotEmpty();
    }

    /**
     * Test que verifica el flujo completo cuando se obtiene una lista
     * paginada de personajes. Esto incluye autenticación JWT, llamada
     * al servicio, petición a SWAPI y transformación de datos.
     */
    @Test
    @DisplayName("Debería obtener lista paginada de personajes con token JWT válido - Flujo completo")
    void testGetAllPeople_WithPagination_ShouldReturnPaginatedList() throws Exception {
        mockMvc.perform(get("/api/v1/people")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageNumber").exists())
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * Test que verifica que podemos buscar un personaje específico por ID.
     * El ID es el UID que usa SWAPI, no un ID interno de nuestra base de datos.
     */
    @Test
    @DisplayName("Debería obtener un personaje por ID con token JWT válido - Flujo completo")
    void testGetPeopleById_ShouldReturnPeople() throws Exception {
        mockMvc.perform(get("/api/v1/people")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uid").exists())
                .andExpect(jsonPath("$.name").exists());
    }

    /**
     * Test que verifica que la seguridad rechaza correctamente las peticiones
     * sin token de autenticación. Esto es importante para asegurar que los
     * endpoints protegidos no sean accesibles sin autenticación.
     */
    @Test
    @DisplayName("Debería rechazar petición sin token JWT - Flujo completo de seguridad")
    void testGetAllPeople_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test que verifica que la seguridad también rechaza tokens inválidos
     * o mal formados. Un token que no sea válido debe resultar en un 401.
     */
    @Test
    @DisplayName("Debería rechazar petición con token JWT inválido - Flujo completo de seguridad")
    void testGetAllPeople_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/people")
                        .header("Authorization", "Bearer token-falso-invalido-12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test que verifica la búsqueda por nombre. Cuando se busca por nombre,
     * el servicio obtiene datos de SWAPI y luego filtra localmente los
     * resultados que coinciden con el nombre buscado.
     */
    @Test
    @DisplayName("Debería buscar personajes por nombre con token JWT válido - Flujo completo")
    void testSearchPeopleByName_ShouldReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/api/v1/people")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("name", "Luke")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * Test que verifica que cuando no se pasan parámetros, se devuelven
     * todos los personajes. Esto requiere hacer múltiples peticiones a
     * SWAPI para obtener todas las páginas, por lo que puede tardar un poco.
     */
    @Test
    @DisplayName("Debería obtener todos los personajes sin parámetros con token JWT válido - Flujo completo")
    void testGetAllPeople_WithoutParams_ShouldReturnAll() throws Exception {
        mockMvc.perform(get("/api/v1/people")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

