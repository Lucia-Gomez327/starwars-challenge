/**
 * TESTS DE SEGURIDAD - FilmController
 * <p>
 * Estos tests verifican que los endpoints de FilmController están protegidos
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// Imports para verificar respuestas HTTP
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Test de seguridad para FilmController
 */
@WebMvcTest(FilmController.class)
class FilmControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;


    /**
     * TEST 1: Acceso sin token JWT
     */
    @Test
    @DisplayName("Debería rechazar acceso sin token JWT - 401 Unauthorized")
    void testGetAllFilms_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TEST 2: Acceso con token inválido
     */
    @Test
    @DisplayName("Debería rechazar acceso con token inválido - 401 Unauthorized")
    void testGetAllFilms_WithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/films")
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
    void testGetAllFilms_WithValidAuthentication_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/films")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

