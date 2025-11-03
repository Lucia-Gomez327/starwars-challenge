/**
 * TEST UNITARIO EDUCATIVO - StarshipService
 * <p>
 * Este archivo contiene tests unitarios para StarshipService.
 * Sigue el mismo patrón que PeopleServiceTest, así que si ya aprendiste esos conceptos,
 * aquí puedes practicar aplicando el mismo conocimiento.
 * <p>
 * CONCEPTOS CLAVE (igual que en PeopleService):
 * - Usamos mocks para aislar StarshipService de SwapiClient y SwapiMapper
 * - Cada test sigue el patrón AAA: Arrange, Act, Assert
 * - Testeamos tanto casos exitosos como casos de error
 */
package com.starwars.application.service;

// Imports de JUnit 5
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

// Imports de Mockito
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Imports de AssertJ
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Imports de Mockito para configurar mocks
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Imports de Spring para paginación
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// Imports de nuestras clases
import com.starwars.domain.model.Starship;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiStarshipDTO;

// Imports de Java estándar
import java.util.List;
import java.util.Optional;

/**
 * Clase de test para StarshipService
 * 
 * Estructura idéntica a PeopleServiceTest:
 * - @ExtendWith para Mockito
 * - @Mock para dependencias
 * - @InjectMocks para el servicio a testear
 */
@ExtendWith(MockitoExtension.class)
class StarshipServiceTest {

    // ========== MOCKS ==========
    // Mismos conceptos que en PeopleService:
    // - swapiClient: simulamos las llamadas a SWAPI
    // - swapiMapper: simulamos la conversión de DTOs
    
    @Mock
    private SwapiClient swapiClient;

    @Mock
    private SwapiMapper swapiMapper;

    // ========== SERVICIO A TESTEAR ==========
    // StarshipService se crea realmente, pero con los mocks inyectados
    
    @InjectMocks
    private StarshipService starshipService;

    /**
     * Método que se ejecuta antes de cada test
     * Por ahora vacío, pero útil si necesitamos datos comunes
     */
    @BeforeEach
    void setUp() {
        // Aquí iría código de preparación común (si lo necesitamos)
    }

    // ========== TESTS ==========

    /**
     * TEST 1: findByUid() - Caso exitoso
     * 
     * Verifica que podemos encontrar una nave espacial por UID cuando existe.
     * Este es el mismo patrón que en PeopleService, pero con Starship.
     */
    @Test
    @DisplayName("Debería encontrar una nave espacial por UID cuando existe en SWAPI")
    void testFindByUid_WhenExists_ShouldReturnStarship() {
        
        // ========== ARRANGE ==========
        // Preparamos los datos de prueba
        
        String uid = "2";  // UID de prueba
        
        // DTO simulado que vendría de SWAPI
        SwapiStarshipDTO swapiDTO = SwapiStarshipDTO.builder()
                .uid("2")
                .name("CR90 corvette")
                .model("CR90 corvette")
                .manufacturer("Corellian Engineering Corporation")
                .costInCredits("3500000")
                .length("150")
                .crew("165")
                .passengers("600")
                .cargoCapacity("3000000")
                .starshipClass("corvette")
                .url("https://www.swapi.tech/api/starships/2")
                .build();
        
        // Objeto Starship esperado después del mapeo
        Starship expectedStarship = Starship.builder()
                .uid("2")
                .name("CR90 corvette")
                .model("CR90 corvette")
                .manufacturer("Corellian Engineering Corporation")
                .costInCredits("3500000")
                .length("150")
                .crew("165")
                .passengers("600")
                .cargoCapacity("3000000")
                .starshipClass("corvette")
                .url("https://www.swapi.tech/api/starships/2")
                .build();
        
        // Configuramos los mocks
        when(swapiClient.fetchById("starships", uid, SwapiStarshipDTO.class))
                .thenReturn(swapiDTO);
        
        when(swapiMapper.toStarship(swapiDTO))
                .thenReturn(expectedStarship);
        
        // ========== ACT ==========
        Optional<Starship> result = starshipService.findByUid(uid);
        
        // ========== ASSERT ==========
        // Verificamos que encontramos la nave espacial
        assertThat(result)
                .isPresent()
                .hasValue(expectedStarship);
        
        // Verificación adicional del nombre
        assertThat(result.get().getName())
                .isEqualTo("CR90 corvette");
    }

    /**
     * TEST 2: findByUid() - Caso cuando no existe
     * 
     * Verifica que cuando buscamos una nave que no existe,
     * el método retorna Optional vacío (no falla).
     */
    @Test
    @DisplayName("Debería retornar Optional vacío cuando la nave espacial no existe en SWAPI")
    void testFindByUid_WhenNotExists_ShouldReturnEmpty() {
        
        // ========== ARRANGE ==========
        String uid = "999";  // UID que no existe
        
        // Configuramos el mock para devolver null (como si SWAPI no encontrara nada)
        when(swapiClient.fetchById("starships", uid, SwapiStarshipDTO.class))
                .thenReturn(null);
        
        // ========== ACT ==========
        Optional<Starship> result = starshipService.findByUid(uid);
        
        // ========== ASSERT ==========
        // Verificamos que el Optional está vacío (no contiene nada)
        assertThat(result)
                .isEmpty()
                .as("Debería retornar Optional vacío cuando la nave no existe");
    }

    /**
     * TEST 3: findAll() - Con paginación
     * 
     * Verifica que podemos obtener una lista paginada de naves espaciales.
     * Testeamos la conversión de paginación y el mapeo de múltiples DTOs.
     */
    @Test
    @DisplayName("Debería retornar lista paginada de naves espaciales desde SWAPI")
    void testFindAll_WithPagination_ShouldReturnPage() {
        
        // ========== ARRANGE ==========
        
        // Paginación de Spring: página 0, tamaño 10
        Pageable pageable = PageRequest.of(0, 10);
        
        // Lista de DTOs simulados (2 naves espaciales)
        List<SwapiStarshipDTO> swapiDTOs = List.of(
                SwapiStarshipDTO.builder()
                        .uid("2")
                        .name("CR90 corvette")
                        .model("CR90 corvette")
                        .build(),
                SwapiStarshipDTO.builder()
                        .uid("3")
                        .name("Star Destroyer")
                        .model("Imperial I-class Star Destroyer")
                        .build()
        );
        
        // Respuesta paginada simulada de SWAPI
        SwapiPageResponse<SwapiStarshipDTO> swapiResponse = SwapiPageResponse.<SwapiStarshipDTO>builder()
                .message("ok")
                .totalRecords(37)  // Total de naves en SWAPI (ejemplo)
                .totalPages(4)     // 37 / 10 = 3.7, redondeado a 4
                .results(swapiDTOs)
                .build();
        
        // Configuramos el mock: Spring página 0 = SWAPI página 1
        when(swapiClient.fetchPage("starships", 1, 10, SwapiStarshipDTO.class))
                .thenReturn(swapiResponse);
        
        // Configuramos el mapper para convertir cualquier DTO
        when(swapiMapper.toStarship(any(SwapiStarshipDTO.class)))
                .thenAnswer(invocation -> {
                    SwapiStarshipDTO dto = invocation.getArgument(0);
                    return Starship.builder()
                            .uid(dto.getUid())
                            .name(dto.getName())
                            .model(dto.getModel())
                            .build();
                });
        
        // ========== ACT ==========
        Page<Starship> result = starshipService.findAll(pageable);
        
        // ========== ASSERT ==========
        
        // Verificamos el tamaño de la página
        assertThat(result.getContent())
                .hasSize(2);
        
        // Verificamos el total de elementos
        assertThat(result.getTotalElements())
                .isEqualTo(37);
        
        // Verificamos que es la primera página
        assertThat(result.isFirst())
                .isTrue();
        
        // Verificamos los nombres
        assertThat(result.getContent())
                .extracting(Starship::getName)
                .containsExactly("CR90 corvette", "Star Destroyer");
    }

    /**
     * TEST 4: findByNameContaining() - Búsqueda exitosa
     * 
     * Verifica que podemos buscar naves espaciales por nombre
     * y que el filtrado funciona correctamente.
     */
    @Test
    @DisplayName("Debería filtrar naves espaciales por nombre y paginar resultados")
    void testFindByNameContaining_ShouldFilterAndPaginate() {
        
        // ========== ARRANGE ==========
        String searchName = "Destroyer";
        Pageable pageable = PageRequest.of(0, 10);
        
        // Simulamos varias naves espaciales de SWAPI
        List<SwapiStarshipDTO> swapiDTOs = List.of(
                SwapiStarshipDTO.builder().uid("3").name("Star Destroyer").build(),
                SwapiStarshipDTO.builder().uid("15").name("Executor").build(),
                SwapiStarshipDTO.builder().uid("27").name("Super Star Destroyer").build()
        );
        
        SwapiPageResponse<SwapiStarshipDTO> swapiResponse = SwapiPageResponse.<SwapiStarshipDTO>builder()
                .results(swapiDTOs)
                .totalRecords(3)
                .build();
        
        // Configuramos los mocks
        when(swapiClient.fetchPage("starships", 1, 100, SwapiStarshipDTO.class))
                .thenReturn(swapiResponse);
        
        when(swapiMapper.toStarship(any(SwapiStarshipDTO.class)))
                .thenAnswer(invocation -> {
                    SwapiStarshipDTO dto = invocation.getArgument(0);
                    return Starship.builder()
                            .uid(dto.getUid())
                            .name(dto.getName())
                            .build();
                });
        
        // ========== ACT ==========
        Page<Starship> result = starshipService.findByNameContaining(searchName, pageable);
        
        // ========== ASSERT ==========
        
        // Verificamos que solo encuentra naves con "Destroyer" en el nombre
        // "Star Destroyer" y "Super Star Destroyer" contienen "Destroyer"
        // "Executor" NO contiene "Destroyer"
        assertThat(result.getContent())
                .hasSize(2)  // Solo 2 naves contienen "Destroyer"
                .extracting(Starship::getName)
                .containsExactly("Star Destroyer", "Super Star Destroyer");
        
        // Verificamos el total de elementos filtrados
        assertThat(result.getTotalElements())
                .isEqualTo(2);
        
        // Verificamos que es la primera página
        assertThat(result.isFirst())
                .isTrue();
    }



}
