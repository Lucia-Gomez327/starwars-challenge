/**
 * TESTS DE SEGURIDAD - VehicleController
 * <p>
 * Estos tests verifican que los endpoints de VehicleController están protegidos
 * correctamente con autenticación JWT.
 * <p>
 * Sigue el mismo patrón que PeopleControllerSecurityTest.
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
import com.starwars.application.mapper.VehicleMapper;
import com.starwars.domain.port.in.VehicleUseCase;
import com.starwars.infrastructure.adapter.in.security.JwtAuthenticationFilter;
import com.starwars.infrastructure.adapter.in.security.JwtTokenProvider;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Test de seguridad para VehicleController
 */
@WebMvcTest(VehicleController.class)
class VehicleControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleUseCase vehicleUseCase;
    
    @MockBean
    private VehicleMapper vehicleMapper;
    
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

    /**
     * TEST 1: Acceso sin token JWT
     */
    @Test
    @DisplayName("Debería rechazar acceso sin token JWT - 401 Unauthorized")
    void testGetAllVehicles_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TEST 2: Acceso con token inválido
     */
    @Test
    @DisplayName("Debería rechazar acceso con token inválido - 401 Unauthorized")
    void testGetAllVehicles_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer token-falso-invalido-12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TEST 3: Acceso con token válido (simulado)
     */
    @Test
    @DisplayName("Debería permitir acceso con autenticación válida - 200 OK")
    @WithMockUser
    void testGetAllVehicles_WithValidAuthentication_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

