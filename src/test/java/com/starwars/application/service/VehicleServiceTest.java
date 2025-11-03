/**
 * TEST UNITARIO EDUCATIVO - VehicleService
 * <p>
 * Este archivo contiene tests unitarios para VehicleService.
 * Sigue el mismo patrón que PeopleServiceTest y StarshipServiceTest.
 * <p>
 * CONCEPTOS CLAVE:
 * - Usamos mocks para aislar VehicleService de SwapiClient y SwapiMapper
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
import com.starwars.domain.model.Vehicle;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.domain.model.SwapiPageResponse;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiVehicleDTO;

// Imports de Java estándar
import java.util.List;
import java.util.Optional;

/**
 * Clase de test para VehicleService
 * 
 * Estructura idéntica a PeopleServiceTest y StarshipServiceTest:
 * - @ExtendWith para Mockito
 * - @Mock para dependencias
 * - @InjectMocks para el servicio a testear
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    // ========== MOCKS ==========
    // Mismos conceptos que en los otros tests:
    // - swapiClient: simulamos las llamadas a SWAPI
    // - swapiMapper: simulamos la conversión de DTOs
    
    @Mock
    private SwapiClient swapiClient;

    @Mock
    private SwapiMapper swapiMapper;

    // ========== SERVICIO A TESTEAR ==========
    // VehicleService se crea realmente, pero con los mocks inyectados
    
    @InjectMocks
    private VehicleService vehicleService;

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
     * Verifica que podemos encontrar un vehículo por UID cuando existe.
     * Mismo patrón que en PeopleService y StarshipService.
     */
    @Test
    @DisplayName("Debería encontrar un vehículo por UID cuando existe en SWAPI")
    void testFindByUid_WhenExists_ShouldReturnVehicle() {
        
        // ========== ARRANGE ==========
        // Preparamos los datos de prueba
        
        String uid = "4";  // UID de prueba
        
        // DTO simulado que vendría de SWAPI
        SwapiVehicleDTO swapiDTO = SwapiVehicleDTO.builder()
                .uid("4")
                .name("Sand Crawler")
                .model("Digger Crawler")
                .manufacturer("Corellia Mining Corporation")
                .costInCredits("150000")
                .length("36.8")
                .crew("46")
                .passengers("30")
                .cargoCapacity("50000")
                .vehicleClass("wheeled")
                .url("https://www.swapi.tech/api/vehicles/4")
                .build();
        
        // Objeto Vehicle esperado después del mapeo
        Vehicle expectedVehicle = Vehicle.builder()
                .uid("4")
                .name("Sand Crawler")
                .model("Digger Crawler")
                .manufacturer("Corellia Mining Corporation")
                .costInCredits("150000")
                .length("36.8")
                .crew("46")
                .passengers("30")
                .cargoCapacity("50000")
                .vehicleClass("wheeled")
                .url("https://www.swapi.tech/api/vehicles/4")
                .build();
        
        // Configuramos los mocks
        when(swapiClient.fetchById("vehicles", uid, SwapiVehicleDTO.class))
                .thenReturn(swapiDTO);
        
        when(swapiMapper.toVehicle(swapiDTO))
                .thenReturn(expectedVehicle);
        
        // ========== ACT ==========
        Optional<Vehicle> result = vehicleService.findByUid(uid);
        
        // ========== ASSERT ==========
        // Verificamos que encontramos el vehículo
        assertThat(result)
                .isPresent()
                .hasValue(expectedVehicle);
        
        // Verificación adicional del nombre
        assertThat(result.get().getName())
                .isEqualTo("Sand Crawler");
    }

    /**
     * TEST 2: findByUid() - Caso cuando no existe
     * 
     * Verifica que cuando buscamos un vehículo que no existe,
     * el método retorna Optional vacío (no falla).
     */
    @Test
    @DisplayName("Debería retornar Optional vacío cuando el vehículo no existe en SWAPI")
    void testFindByUid_WhenNotExists_ShouldReturnEmpty() {
        
        // ========== ARRANGE ==========
        String uid = "999";  // UID que no existe
        
        // Configuramos el mock para devolver null (como si SWAPI no encontrara nada)
        when(swapiClient.fetchById("vehicles", uid, SwapiVehicleDTO.class))
                .thenReturn(null);
        
        // ========== ACT ==========
        Optional<Vehicle> result = vehicleService.findByUid(uid);
        
        // ========== ASSERT ==========
        // Verificamos que el Optional está vacío (no contiene nada)
        assertThat(result)
                .isEmpty()
                .as("Debería retornar Optional vacío cuando el vehículo no existe");
    }

    /**
     * TEST 3: findAll() - Con paginación
     * 
     * Verifica que podemos obtener una lista paginada de vehículos.
     * Testeamos la conversión de paginación y el mapeo de múltiples DTOs.
     */
    @Test
    @DisplayName("Debería retornar lista paginada de vehículos desde SWAPI")
    void testFindAll_WithPagination_ShouldReturnPage() {
        
        // ========== ARRANGE ==========
        
        // Paginación de Spring: página 0, tamaño 10
        Pageable pageable = PageRequest.of(0, 10);
        
        // Lista de DTOs simulados (2 vehículos)
        List<SwapiVehicleDTO> swapiDTOs = List.of(
                SwapiVehicleDTO.builder()
                        .uid("4")
                        .name("Sand Crawler")
                        .model("Digger Crawler")
                        .build(),
                SwapiVehicleDTO.builder()
                        .uid("6")
                        .name("T-16 skyhopper")
                        .model("T-16 skyhopper")
                        .build()
        );
        
        // Respuesta paginada simulada de SWAPI
        SwapiPageResponse<SwapiVehicleDTO> swapiResponse = SwapiPageResponse.<SwapiVehicleDTO>builder()
                .message("ok")
                .totalRecords(39)  // Total de vehículos en SWAPI (ejemplo)
                .totalPages(4)     // 39 / 10 = 3.9, redondeado a 4
                .results(swapiDTOs)
                .build();
        
        // Configuramos el mock: Spring página 0 = SWAPI página 1
        when(swapiClient.fetchPage("vehicles", 1, 10, SwapiVehicleDTO.class))
                .thenReturn(swapiResponse);
        
        // Configuramos el mapper para convertir cualquier DTO
        when(swapiMapper.toVehicle(any(SwapiVehicleDTO.class)))
                .thenAnswer(invocation -> {
                    SwapiVehicleDTO dto = invocation.getArgument(0);
                    return Vehicle.builder()
                            .uid(dto.getUid())
                            .name(dto.getName())
                            .model(dto.getModel())
                            .build();
                });
        
        // ========== ACT ==========
        Page<Vehicle> result = vehicleService.findAll(pageable);
        
        // ========== ASSERT ==========
        
        // Verificamos el tamaño de la página
        assertThat(result.getContent())
                .hasSize(2);
        
        // Verificamos el total de elementos
        assertThat(result.getTotalElements())
                .isEqualTo(39);
        
        // Verificamos que es la primera página
        assertThat(result.isFirst())
                .isTrue();
        
        // Verificamos los nombres
        assertThat(result.getContent())
                .extracting(Vehicle::getName)
                .containsExactly("Sand Crawler", "T-16 skyhopper");
    }

    /**
     * TEST 4: findByNameContaining() - Búsqueda exitosa
     * 
     * Verifica que podemos buscar vehículos por nombre
     * y que el filtrado funciona correctamente.
     */
    @Test
    @DisplayName("Debería filtrar vehículos por nombre y paginar resultados")
    void testFindByNameContaining_ShouldFilterAndPaginate() {
        
        // ========== ARRANGE ==========
        String searchName = "Crawler";
        Pageable pageable = PageRequest.of(0, 10);
        
        // Simulamos varios vehículos de SWAPI
        List<SwapiVehicleDTO> swapiDTOs = List.of(
                SwapiVehicleDTO.builder().uid("4").name("Sand Crawler").build(),
                SwapiVehicleDTO.builder().uid("19").name("X-34 landspeeder").build(),
                SwapiVehicleDTO.builder().uid("38").name("Crawler").build()
        );
        
        SwapiPageResponse<SwapiVehicleDTO> swapiResponse = SwapiPageResponse.<SwapiVehicleDTO>builder()
                .results(swapiDTOs)
                .totalRecords(3)
                .build();
        
        // Configuramos los mocks
        when(swapiClient.fetchPage("vehicles", 1, 100, SwapiVehicleDTO.class))
                .thenReturn(swapiResponse);
        
        when(swapiMapper.toVehicle(any(SwapiVehicleDTO.class)))
                .thenAnswer(invocation -> {
                    SwapiVehicleDTO dto = invocation.getArgument(0);
                    return Vehicle.builder()
                            .uid(dto.getUid())
                            .name(dto.getName())
                            .build();
                });
        
        // ========== ACT ==========
        Page<Vehicle> result = vehicleService.findByNameContaining(searchName, pageable);
        
        // ========== ASSERT ==========
        
        // Verificamos que solo encuentra vehículos con "Crawler" en el nombre
        // "Sand Crawler" y "Crawler" contienen "Crawler"
        // "X-34 landspeeder" NO contiene "Crawler"
        assertThat(result.getContent())
                .hasSize(2)  // Solo 2 vehículos contienen "Crawler"
                .extracting(Vehicle::getName)
                .containsExactly("Sand Crawler", "Crawler");
        
        // Verificamos el total de elementos filtrados
        assertThat(result.getTotalElements())
                .isEqualTo(2);
        
        // Verificamos que es la primera página
        assertThat(result.isFirst())
                .isTrue();
    }

}

