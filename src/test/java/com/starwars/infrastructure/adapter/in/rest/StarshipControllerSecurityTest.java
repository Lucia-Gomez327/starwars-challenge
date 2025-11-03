/**
 * TESTS DE SEGURIDAD - StarshipController
 * <p>
 * Estos tests verifican que los endpoints de StarshipController están protegidos
 * correctamente con autenticación JWT.
 * <p>
 * Sigue el mismo patrón que PeopleControllerSecurityTest.
 * La seguridad funciona igual para todos los controladores.
 */
package com.starwars.infrastructure.adapter.in.rest;

// Imports de JUnit 5
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// Imports para verificar respuestas HTTP
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Imports de nuestras clases que necesitamos mockear
import com.starwars.application.mapper.StarshipMapper;
import com.starwars.domain.port.in.StarshipUseCase;
import com.starwars.infrastructure.adapter.in.security.JwtAuthenticationFilter;
import com.starwars.infrastructure.adapter.in.security.JwtTokenProvider;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Test de seguridad para StarshipController
 * 
 * Mismo patrón que PeopleControllerSecurityTest:
 * - @WebMvcTest carga solo el contexto web necesario
 * - MockMvc simula peticiones HTTP
 * - @MockBean para las dependencias
 */
@WebMvcTest(StarshipController.class)
class StarshipControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StarshipUseCase starshipUseCase;
    
    @MockBean
    private StarshipMapper starshipMapper;
    
    @MockBean
    private SwapiClient swapiClient;
    
    @MockBean
    private SwapiMapper swapiMapper;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private UserDetailsService userDetailsService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // ========== TESTS DE SEGURIDAD ==========

    /**
     * TEST 1: Acceso sin token JWT
     * 
     * Verifica que sin token, el acceso es rechazado con 401.
     */
    @Test
    @DisplayName("Debería rechazar acceso sin token JWT - 401 Unauthorized")
    void testGetAllStarships_WithoutToken_ShouldReturn401() throws Exception {
        
        // ========== ACT y ASSERT ==========
        // Hacemos petición GET sin header Authorization
        // Esperamos 401 Unauthorized
        mockMvc.perform(get("/api/v1/starships")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());  // Verifica código 401
    }

    /**
     * TEST 2: Acceso con token inválido
     * 
     * Verifica que con token falso, el acceso es rechazado con 401.
     */
    @Test
    @DisplayName("Debería rechazar acceso con token inválido - 401 Unauthorized")
    void testGetAllStarships_WithInvalidToken_ShouldReturn401() throws Exception {
        
        // ========== ACT y ASSERT ==========
        // Hacemos petición GET con token inválido
        // Esperamos 401 Unauthorized
        mockMvc.perform(get("/api/v1/starships")
                        .header("Authorization", "Bearer token-falso-invalido-12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());  // Verifica código 401
    }

    /**
     * TEST 3: Acceso con token válido (simulado)
     * 
     * Verifica que con autenticación válida, el acceso es permitido.
     */
    @Test
    @DisplayName("Debería permitir acceso con autenticación válida - 200 OK")
    @WithMockUser  // Simula usuario autenticado
    void testGetAllStarships_WithValidAuthentication_ShouldReturn200() throws Exception {
        
        // ========== ACT y ASSERT ==========
        // Con @WithMockUser, estamos autenticados
        // Esperamos 200 OK (el endpoint funciona)
        mockMvc.perform(get("/api/v1/starships")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());  // Verifica código 200
    }
}

