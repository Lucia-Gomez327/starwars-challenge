/**
 * TESTS DE SEGURIDAD - PeopleController
 * <p>
 * Estos tests verifican que los endpoints están protegidos correctamente con autenticación JWT.
 * <p>
 * ¿EN QUÉ SE DIFERENCIAN DE LOS TESTS UNITARIOS DE SERVICIOS?
 * - Tests unitarios: Testean la lógica de negocio AISLADA (con mocks)
 * - Tests de seguridad: Testean que los endpoints HTTP están protegidos correctamente
 * <p>
 * ¿QUÉ TESTEAMOS AQUÍ?
 * 1. Acceso sin token → debe rechazar (401 Unauthorized)
 * 2. Acceso con token inválido → debe rechazar (401 Unauthorized)
 * 3. Acceso con token válido → debe permitir (200 OK)
 * <p>
 * CONCEPTOS CLAVE:
 * - @WebMvcTest: Carga solo el contexto web (controladores, seguridad), más rápido que @SpringBootTest
 * - MockMvc: Simula peticiones HTTP reales (GET, POST, etc.)
 * - @MockBean: Crea mocks de beans de Spring (similar a @Mock pero integrado con Spring)
 * - @WithMockUser: Simula un usuario autenticado para tests que necesitan autenticación
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
import com.starwars.application.mapper.PeopleMapper;
import com.starwars.domain.port.in.PeopleUseCase;
import com.starwars.infrastructure.adapter.in.security.JwtAuthenticationFilter;
import com.starwars.infrastructure.adapter.in.security.JwtTokenProvider;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Test de seguridad para PeopleController
 * 
 * @WebMvcTest(PeopleController.class)
 * Esto le dice a Spring:
 * - "Carga solo el contexto web necesario para testear PeopleController"
 * - "NO cargues toda la aplicación (no bases de datos, no servicios completos, etc.)"
 * - "SÍ carga: controladores, configuración de seguridad, filtros"
 * 
 * Esto hace los tests más rápidos porque no carga todo el contexto de Spring.
 */
@WebMvcTest(PeopleController.class)
class PeopleControllerSecurityTest {

    /**
     * MockMvc simula peticiones HTTP reales.
     * 
     * ¿QUÉ ES MockMvc?
     * MockMvc permite simular peticiones HTTP (GET, POST, PUT, DELETE)
     * sin necesidad de levantar un servidor real.
     * 
     * Es como hacer una petición HTTP real, pero todo se simula:
     * - No hay red real
     * - No hay servidor HTTP real
     * - Todo se ejecuta en memoria
     * 
     * Ventajas:
     * - Muy rápido (no hay overhead de red)
     * - Verificamos respuestas HTTP (códigos de estado, headers, body)
     * - Podemos testear la seguridad completa
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * @MockBean crea mocks de beans de Spring
     * 
     * ¿QUÉ ES @MockBean?
     * Similar a @Mock en tests unitarios, pero integrado con Spring.
     * Spring usa estos mocks en lugar de los beans reales.
     * 
     * ¿POR QUÉ NECESITAMOS MOCKEAR ESTOS?
     * PeopleController depende de estos componentes.
     * Para testear solo la seguridad, no necesitamos que funcionen realmente,
     * solo necesitamos que existan para que el controlador se pueda crear.
     */
    @MockBean
    private PeopleUseCase peopleUseCase;
    
    @MockBean
    private PeopleMapper peopleMapper;
    
    @MockBean
    private SwapiClient swapiClient;
    
    @MockBean
    private SwapiMapper swapiMapper;
    
    /**
     * También necesitamos mockear los componentes de seguridad
     * porque Spring Security los necesita para funcionar.
     */
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
     * Este test verifica que cuando intentamos acceder a un endpoint protegido
     * SIN token de autenticación, Spring Security rechaza el acceso.
     * <p>
     * ¿QUÉ DEBE PASAR?
     * - Hacemos una petición GET sin header "Authorization"
     * - Spring Security detecta que no hay token
     * - Responde con 401 Unauthorized (No autorizado)
     */
    @Test
    @DisplayName("Debería rechazar acceso sin token JWT - 401 Unauthorized")
    void testGetAllPeople_WithoutToken_ShouldReturn401() throws Exception {
        
        // ========== ACT y ASSERT ==========
        // 
        // mockMvc.perform() inicia una petición HTTP simulada
        // get("/api/v1/people") hace un GET a /api/v1/people
        // contentType(MediaType.APPLICATION_JSON) especifica que enviamos JSON
        // 
        // .andExpect() verifica la respuesta HTTP
        // status().isUnauthorized() verifica que el código de estado sea 401
        // 
        // ¿POR QUÉ NO PONEMOS HEADER "Authorization"?
        // Eso es exactamente lo que queremos testear:
        // "¿Qué pasa si NO ponemos token?"
        // 
        // Resultado esperado: 401 Unauthorized
        mockMvc.perform(get("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());  // Verifica código 401
    }

    /**
     * TEST 2: Acceso con token inválido
     * 
     * Este test verifica que cuando intentamos acceder con un token falso/inválido,
     * Spring Security rechaza el acceso.
     * <p>
     * ¿QUÉ DEBE PASAR?
     * - Hacemos una petición GET con header "Authorization: Bearer token-falso"
     * - JwtTokenProvider intenta validar el token y falla
     * - Spring Security detecta que el token no es válido
     * - Responde con 401 Unauthorized
     */
    @Test
    @DisplayName("Debería rechazar acceso con token inválido - 401 Unauthorized")
    void testGetAllPeople_WithInvalidToken_ShouldReturn401() throws Exception {
        
        // ========== ACT y ASSERT ==========
        // 
        // header("Authorization", "Bearer token-falso") agrega el header de autorización
        // con un token que sabemos que es inválido
        // 
        // Este token no está firmado correctamente, está expirado, o es completamente falso.
        // JwtTokenProvider.validateToken() debe devolver false.
        // 
        // Resultado esperado: 401 Unauthorized
        mockMvc.perform(get("/api/v1/people")
                        .header("Authorization", "Bearer token-falso-invalido-12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());  // Verifica código 401
    }

    /**
     * TEST 3: Acceso con token con formato incorrecto
     * 
     * Este test verifica que cuando el token no tiene el formato correcto
     * (por ejemplo, falta "Bearer " al inicio), se rechaza.
     */
    @Test
    @DisplayName("Debería rechazar acceso con token sin formato Bearer - 401 Unauthorized")
    void testGetAllPeople_WithTokenWithoutBearer_ShouldReturn401() throws Exception {
        
        // ========== ACT y ASSERT ==========
        // 
        // Intentamos poner un token sin "Bearer " al inicio
        // JwtAuthenticationFilter.getJwtFromRequest() espera "Bearer " al inicio
        // Si no lo tiene, devuelve null y el token no se procesa
        // 
        // Resultado esperado: 401 Unauthorized
        mockMvc.perform(get("/api/v1/people")
                        .header("Authorization", "token-sin-bearer-12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());  // Verifica código 401
    }

    /**
     * TEST 4: Acceso con token válido (simulado)
     * 
     * Este test verifica que cuando accedemos con un usuario autenticado,
     * el endpoint funciona correctamente.
     * <p>
     * ¿CÓMO SIMULAMOS UN TOKEN VÁLIDO?
     * Usamos @WithMockUser que le dice a Spring Security:
     * "Simula que hay un usuario autenticado para este test"
     * 
     * No necesitamos un token real, Spring Security simula la autenticación.
     * 
     * ¿QUÉ DEBE PASAR?
     * - @WithMockUser simula un usuario autenticado
     * - Spring Security permite el acceso
     * - El endpoint se ejecuta (aunque el mock puede devolver datos)
     * - Responde con 200 OK o el código que corresponda
     */
    @Test
    @DisplayName("Debería permitir acceso con autenticación válida - 200 OK")
    @WithMockUser  // Spring Security simula un usuario autenticado
    void testGetAllPeople_WithValidAuthentication_ShouldReturn200() throws Exception {
        
        // ========== ACT y ASSERT ==========
        // 
        // Con @WithMockUser, Spring Security simula que hay un usuario autenticado.
        // No necesitamos agregar el header "Authorization" manualmente,
        // Spring Security ya sabe que estamos autenticados.
        // 
        // El endpoint se ejecutará normalmente.
        // Como mockeamos PeopleUseCase, puede que devuelva datos vacíos,
        // pero lo importante es que NO recibimos 401 (estamos autenticados).
        // 
        // Resultado esperado: 200 OK (o el código que devuelva el endpoint)
        mockMvc.perform(get("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());  // Verifica código 200
    }

    /**
     * TEST 5: Acceso a endpoint sin parámetros
     * 
     * Verifica que también funciona para diferentes variantes del endpoint.
     */
    @Test
    @DisplayName("Debería rechazar acceso sin token incluso sin parámetros")
    void testGetAllPeople_WithoutToken_NoParams_ShouldReturn401() throws Exception {
        
        // ========== ACT y ASSERT ==========
        // Mismo test pero sin parámetros explícitos
        mockMvc.perform(get("/api/v1/people"))
                .andExpect(status().isUnauthorized());  // Verifica código 401
    }
}

