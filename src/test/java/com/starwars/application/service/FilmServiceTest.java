/**
 * TEST UNITARIO EDUCATIVO - FilmService
 * <p>
 * Este archivo contiene tests unitarios para FilmService.
 * Sigue el mismo patrón que PeopleServiceTest, StarshipServiceTest y VehicleServiceTest.
 * <p>
 * CONCEPTOS CLAVE:
 * - Usamos mocks para aislar FilmService de SwapiClient y SwapiMapper
 * - Cada test sigue el patrón AAA: Arrange, Act, Assert
 * - Testeamos tanto casos exitosos como casos de error
 * <p>
 * NOTA: FilmService usa "title" en lugar de "name" para las películas,
 * pero el concepto es exactamente el mismo que en los otros servicios.
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
import com.starwars.domain.model.Film;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiFilmDTO;

// Imports de Java estándar
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Clase de test para FilmService
 * 
 * Estructura idéntica a PeopleServiceTest, StarshipServiceTest y VehicleServiceTest:
 * - @ExtendWith para Mockito
 * - @Mock para dependencias
 * - @InjectMocks para el servicio a testear
 */
@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    // ========== MOCKS ==========
    // Mismos conceptos que en los otros tests:
    // - swapiClient: simulamos las llamadas a SWAPI
    // - swapiMapper: simulamos la conversión de DTOs
    
    @Mock
    private SwapiClient swapiClient;

    @Mock
    private SwapiMapper swapiMapper;

    // ========== SERVICIO A TESTEAR ==========
    // FilmService se crea realmente, pero con los mocks inyectados
    
    @InjectMocks
    private FilmService filmService;

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
     * Verifica que podemos encontrar una película por UID cuando existe.
     * Mismo patrón que en PeopleService, StarshipService y VehicleService.
     * <p>
     * NOTA: Las películas tienen "title" en lugar de "name",
     * pero el concepto es exactamente el mismo.
     */
    @Test
    @DisplayName("Debería encontrar una película por UID cuando existe en SWAPI")
    void testFindByUid_WhenExists_ShouldReturnFilm() {
        
        // ========== ARRANGE ==========
        // Preparamos los datos de prueba
        
        String uid = "1";  // UID de prueba
        
        // DTO simulado que vendría de SWAPI
        // NOTA: SwapiFilmDTO usa LocalDate para releaseDate
        SwapiFilmDTO swapiDTO = SwapiFilmDTO.builder()
                .uid("1")
                .title("A New Hope")
                .episodeId(4)
                .openingCrawl("It is a period of civil war...")
                .director("George Lucas")
                .producer("Gary Kurtz, Rick McCallum")
                .releaseDate(LocalDate.of(1977, 5, 25))
                .url("https://www.swapi.tech/api/films/1")
                .build();
        
        // Objeto Film esperado después del mapeo
        Film expectedFilm = Film.builder()
                .uid("1")
                .title("A New Hope")
                .episodeId(4)
                .openingCrawl("It is a period of civil war...")
                .director("George Lucas")
                .producer("Gary Kurtz, Rick McCallum")
                .releaseDate(LocalDate.of(1977, 5, 25))
                .url("https://www.swapi.tech/api/films/1")
                .build();
        
        // Configuramos los mocks
        when(swapiClient.fetchById("films", uid, SwapiFilmDTO.class))
                .thenReturn(swapiDTO);
        
        when(swapiMapper.toFilm(swapiDTO))
                .thenReturn(expectedFilm);
        
        // ========== ACT ==========
        Optional<Film> result = filmService.findByUid(uid);
        
        // ========== ASSERT ==========
        // Verificamos que encontramos la película
        assertThat(result)
                .isPresent()
                .hasValue(expectedFilm);
        
        // Verificación adicional del título
        assertThat(result.get().getTitle())
                .isEqualTo("A New Hope");
        
        // Verificación adicional del episodeId
        assertThat(result.get().getEpisodeId())
                .isEqualTo(4);
    }

    /**
     * TEST 2: findByUid() - Caso cuando no existe
     * 
     * Verifica que cuando buscamos una película que no existe,
     * el método retorna Optional vacío (no falla).
     */
    @Test
    @DisplayName("Debería retornar Optional vacío cuando la película no existe en SWAPI")
    void testFindByUid_WhenNotExists_ShouldReturnEmpty() {
        
        // ========== ARRANGE ==========
        String uid = "999";  // UID que no existe
        
        // Configuramos el mock para devolver null (como si SWAPI no encontrara nada)
        when(swapiClient.fetchById("films", uid, SwapiFilmDTO.class))
                .thenReturn(null);
        
        // ========== ACT ==========
        Optional<Film> result = filmService.findByUid(uid);
        
        // ========== ASSERT ==========
        // Verificamos que el Optional está vacío (no contiene nada)
        assertThat(result)
                .isEmpty()
                .as("Debería retornar Optional vacío cuando la película no existe");
    }

    /**
     * TEST 3: findAll() - Con paginación
     * 
     * Verifica que podemos obtener una lista paginada de películas.
     * Testeamos la conversión de paginación y el mapeo de múltiples DTOs.
     */
    @Test
    @DisplayName("Debería retornar lista paginada de películas desde SWAPI")
    void testFindAll_WithPagination_ShouldReturnPage() {
        
        // ========== ARRANGE ==========
        
        // Paginación de Spring: página 0, tamaño 10
        Pageable pageable = PageRequest.of(0, 10);
        
        // Lista de DTOs simulados (2 películas)
        // NOTA: Las películas tienen menos datos que personajes/naves/vehículos
        List<SwapiFilmDTO> swapiDTOs = List.of(
                SwapiFilmDTO.builder()
                        .uid("1")
                        .title("A New Hope")
                        .episodeId(4)
                        .director("George Lucas")
                        .releaseDate(LocalDate.of(1977, 5, 25))
                        .build(),
                SwapiFilmDTO.builder()
                        .uid("2")
                        .title("The Empire Strikes Back")
                        .episodeId(5)
                        .director("Irvin Kershner")
                        .releaseDate(LocalDate.of(1980, 5, 21))
                        .build()
        );
        
        // Respuesta paginada simulada de SWAPI
        SwapiPageResponse<SwapiFilmDTO> swapiResponse = SwapiPageResponse.<SwapiFilmDTO>builder()
                .message("ok")
                .totalRecords(6)  // Total de películas en SWAPI (hay 6 películas)
                .totalPages(1)    // 6 / 10 = 0.6, redondeado a 1 página
                .results(swapiDTOs)
                .build();
        
        // Configuramos el mock: Spring página 0 = SWAPI página 1
        when(swapiClient.fetchPage("films", 1, 10, SwapiFilmDTO.class))
                .thenReturn(swapiResponse);
        
        // Configuramos el mapper para convertir cualquier DTO
        when(swapiMapper.toFilm(any(SwapiFilmDTO.class)))
                .thenAnswer(invocation -> {
                    SwapiFilmDTO dto = invocation.getArgument(0);
                    return Film.builder()
                            .uid(dto.getUid())
                            .title(dto.getTitle())
                            .episodeId(dto.getEpisodeId())
                            .director(dto.getDirector())
                            .releaseDate(dto.getReleaseDate())
                            .build();
                });
        
        // ========== ACT ==========
        Page<Film> result = filmService.findAll(pageable);
        
        // ========== ASSERT ==========
        
        // Verificamos el tamaño de la página
        assertThat(result.getContent())
                .hasSize(2);
        
        // Verificamos el total de elementos
        assertThat(result.getTotalElements())
                .isEqualTo(6);
        
        // Verificamos que es la primera página
        assertThat(result.isFirst())
                .isTrue();
        
        // Verificamos los títulos
        assertThat(result.getContent())
                .extracting(Film::getTitle)
                .containsExactly("A New Hope", "The Empire Strikes Back");
        
        // Verificación adicional: verificar los episodeId
        assertThat(result.getContent())
                .extracting(Film::getEpisodeId)
                .containsExactly(4, 5);
    }

    /**
     * TEST 4: findByTitleContaining() - Búsqueda exitosa
     * 
     * Verifica que podemos buscar películas por título
     * y que el filtrado funciona correctamente.
     * <p>
     * NOTA: FilmService usa findByTitleContaining() en lugar de findByNameContaining(),
     * porque las películas tienen "title", no "name".
     */
    @Test
    @DisplayName("Debería filtrar películas por título y paginar resultados")
    void testFindByTitleContaining_ShouldFilterAndPaginate() {
        
        // ========== ARRANGE ==========
        String searchTitle = "Hope";  // Buscamos películas que contengan "Hope"
        Pageable pageable = PageRequest.of(0, 10);
        
        // Simulamos varias películas de SWAPI
        List<SwapiFilmDTO> swapiDTOs = List.of(
                SwapiFilmDTO.builder()
                        .uid("1")
                        .title("A New Hope")
                        .episodeId(4)
                        .build(),
                SwapiFilmDTO.builder()
                        .uid("4")
                        .title("The Phantom Menace")
                        .episodeId(1)
                        .build(),
                SwapiFilmDTO.builder()
                        .uid("5")
                        .title("Attack of the Clones")
                        .episodeId(2)
                        .build()
        );
        
        SwapiPageResponse<SwapiFilmDTO> swapiResponse = SwapiPageResponse.<SwapiFilmDTO>builder()
                .results(swapiDTOs)
                .totalRecords(3)
                .build();
        
        // Configuramos los mocks
        when(swapiClient.fetchPage("films", 1, 100, SwapiFilmDTO.class))
                .thenReturn(swapiResponse);
        
        when(swapiMapper.toFilm(any(SwapiFilmDTO.class)))
                .thenAnswer(invocation -> {
                    SwapiFilmDTO dto = invocation.getArgument(0);
                    return Film.builder()
                            .uid(dto.getUid())
                            .title(dto.getTitle())
                            .episodeId(dto.getEpisodeId())
                            .build();
                });
        
        // ========== ACT ==========
        // Ejecutamos el método que busca por título
        // Este método debería:
        // 1. Obtener todas las películas de SWAPI (simuladas)
        // 2. Filtrarlas por título que contenga "Hope"
        // 3. Paginar los resultados
        Page<Film> result = filmService.findByTitleContaining(searchTitle, pageable);
        
        // ========== ASSERT ==========
        
        // Verificamos que solo encuentra películas con "Hope" en el título
        // "A New Hope" contiene "Hope" ✓
        // "The Phantom Menace" NO contiene "Hope" ✗
        // "Attack of the Clones" NO contiene "Hope" ✗
        assertThat(result.getContent())
                .hasSize(1)  // Solo debería encontrar "A New Hope"
                .extracting(Film::getTitle)
                .containsExactly("A New Hope");
        
        // Verificamos el total de elementos filtrados
        assertThat(result.getTotalElements())
                .isEqualTo(1);
        
        // Verificamos que es la primera página
        assertThat(result.isFirst())
                .isTrue();
        
        // Verificación adicional: verificar el episodeId
        assertThat(result.getContent().get(0).getEpisodeId())
                .isEqualTo(4);
    }

    /**
     * TEST 5: save() - Caso de error esperado
     * 
     * Verifica que save() lanza una excepción porque SWAPI es solo lectura.
     * No podemos guardar nuevas películas en SWAPI.
     */
    @Test
    @DisplayName("Debería lanzar UnsupportedOperationException cuando se intenta guardar")
    void testSave_ShouldThrowException() {
        
        // ========== ARRANGE ==========
        Film film = Film.builder()
                .uid("1")
                .title("Test Film")
                .episodeId(7)
                .build();
        
        // ========== ACT y ASSERT ==========
        // Verificamos que se lanza la excepción esperada
        assertThatThrownBy(() -> filmService.save(film))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not supported")
                .as("Debería lanzar UnsupportedOperationException porque SWAPI es solo lectura");
    }
}

