/**
 * TEST UNITARIO EDUCATIVO - PeopleService
 * <p>
 * Este archivo contiene tests unitarios para PeopleService.
 * <p>
 * ¿QUÉ ES UN TEST UNITARIO?
 * Un test unitario verifica que una "unidad" de código funciona correctamente.
 * Una unidad puede ser un método, una clase, o un pequeño componente.
 * Lo importante es que testeamos la unidad AISLADA de sus dependencias.
 * <p>
 * ¿POR QUÉ USAMOS MOCKS?
 * PeopleService depende de SwapiClient y SwapiMapper.
 * En lugar de llamar al API real de SWAPI (que sería lento y no confiable),
 * creamos "mocks" (simulaciones) que se comportan como queremos.
 * <p>
 * ESTRUCTURA AAA:
 * Cada test sigue el patrón AAA:
 * - ARRANGE (Preparar): Preparamos datos de prueba y configuramos mocks
 * - ACT (Ejecutar): Ejecutamos el método que queremos testear
 * - ASSERT (Verificar): Verificamos que el resultado es el esperado
 */

// Este paquete agrupa todos los tests del proyecto
// La estructura de paquetes en test debe reflejar la estructura en main
package com.starwars.application.service;

// ========== IMPORTS NECESARIOS ==========
// Estos son todos los imports que necesitamos para los tests

// Imports de JUnit 5 (el framework de testing)

import org.junit.jupiter.api.BeforeEach;  // Para código que se ejecuta antes de cada test
import org.junit.jupiter.api.DisplayName;  // Para dar nombres legibles a los tests
import org.junit.jupiter.api.Test;  // Para marcar métodos como tests

// Imports de Mockito (para crear mocks)
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;  // Inyecta mocks en la clase a testear
import org.mockito.Mock;  // Crea un objeto simulado (mock)
import org.mockito.junit.jupiter.MockitoExtension;  // Integración de Mockito con JUnit 5

// Imports de AssertJ (para hacer verificaciones más legibles)
import static org.assertj.core.api.Assertions.assertThat;  // Para verificaciones normales
import static org.assertj.core.api.Assertions.assertThatThrownBy;  // Para verificar excepciones

// Imports de Mockito para configurar comportamiento de mocks
import static org.mockito.ArgumentMatchers.any;  // Para "cualquier objeto de este tipo"
import static org.mockito.ArgumentMatchers.eq;  // Para valores específicos (equals)
import static org.mockito.Mockito.when;  // Para configurar qué debe devolver un mock

// Imports de Spring para paginación
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// Imports de nuestras clases
import com.starwars.domain.model.People;
import com.starwars.domain.port.out.SwapiClient;
import com.starwars.infrastructure.adapter.out.client.SwapiMapper;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiPageResponse;
import com.starwars.infrastructure.adapter.out.client.dto.SwapiPeopleDTO;

// Imports de Java estándar
import java.util.List;
import java.util.Optional;

/**
 * Clase de test para PeopleService
 *
 * @ExtendWith(MockitoExtension.class)
 * Esto le dice a JUnit que use Mockito para los mocks.
 * Sin esta anotación, las anotaciones @Mock y @InjectMocks no funcionarían.
 * Es como decirle a JUnit: "Usa Mockito como extensión para manejar los mocks"
 */
@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    // ========== DECLARACIÓN DE MOCKS ==========
    // 
    // ¿QUÉ ES UN MOCK?
    // Un mock es un objeto "simulado" que imita el comportamiento de un objeto real.
    // Le decimos al mock cómo debe comportarse: qué debe devolver cuando se le llama.
    // Es como un actor que sigue un guión: le damos las líneas y él las dice.
    //
    // ¿POR QUÉ USAMOS MOCKS AQUÍ?
    // PeopleService necesita SwapiClient y SwapiMapper.
    // Si usáramos los objetos reales, tendríamos que:
    // - Conectarnos al API real de SWAPI (lento, depende de internet)
    // - Usar datos reales que pueden cambiar
    // - Hacer tests más lentos y frágiles
    // 
    // Con mocks, controlamos exactamente qué devuelven, y los tests son:
    // - Rápidos (no hay llamadas de red)
    // - Confiables (siempre devuelven lo mismo)
    // - Aislados (no dependen de servicios externos)

    /**
     * @Mock crea un "objeto simulado" de SwapiClient.
     *
     * Esto crea un objeto que PARECE ser un SwapiClient,
     * pero no es el real. Es una "simulación" vacía.
     *
     * Después, en cada test, le diremos qué debe devolver
     * cuando se le llame a sus métodos.
     *
     * Es como tener un actor que espera su guión:
     * cuando le digamos "cuando alguien te pregunte X, responde Y",
     * él seguirá ese guión.
     */
    @Mock
    private SwapiClient swapiClient;

    /**
     * @Mock crea otro objeto simulado, esta vez de SwapiMapper.
     *
     * El mapper convierte DTOs de SWAPI a nuestros modelos de dominio.
     * En los tests, simularemos esa conversión.
     */
    @Mock
    private SwapiMapper swapiMapper;

    // ========== DECLARACIÓN DEL SERVICIO A TESTEAR ==========

    /**
     * @InjectMocks crea una instancia REAL de PeopleService.
     *
     * Esto es diferente a @Mock:
     * - @Mock crea un objeto SIMULADO (vacío, no hace nada real)
     * - @InjectMocks crea un objeto REAL de PeopleService
     *
     * Lo que hace @InjectMocks es:
     * 1. Crear una instancia real de PeopleService
     * 2. Inyectar los mocks de arriba (@Mock) en lugar de crear objetos reales
     *
     * Entonces PeopleService funcionará normalmente,
     * pero cuando llame a swapiClient o swapiMapper,
     * estará usando nuestros mocks (no los objetos reales).
     *
     * Esto nos permite testear PeopleService de forma AISLADA:
     * solo testeamos la lógica de PeopleService,
     * no la de SwapiClient o SwapiMapper.
     */
    @InjectMocks
    private PeopleService peopleService;

    // ========== TESTS ==========
    //
    // Cada método marcado con @Test es un test individual.
    // Cada test debe:
    // 1. Tener un nombre descriptivo que explique qué está testeando
    // 2. Seguir el patrón AAA (Arrange, Act, Assert)
    // 3. Testear UN caso específico (no varios casos en un test)

    /**
     * TEST 1: findByUid() - Caso exitoso
     *
     * Este test verifica que cuando buscamos un personaje por UID
     * y ese personaje existe en SWAPI, el método lo encuentra correctamente.
     *
     * ¿QUÉ ESTAMOS TESTEANDO?
     * - Que findByUid() puede encontrar un personaje cuando existe
     * - Que el mapeo de DTO a People funciona correctamente
     * - Que el resultado es un Optional con el personaje dentro
     */
    @Test
    // @DisplayName nos permite darle un nombre legible al test
    // Esto aparece en los reportes de test, es más descriptivo que el nombre del método
    @DisplayName("Debería encontrar un personaje por UID cuando existe en SWAPI")
    void testFindByUid_WhenExists_ShouldReturnPeople() {

        // ========== ARRANGE (PREPARAR) ==========
        // En esta sección preparamos TODO lo necesario para el test:
        // - Datos de prueba (qué UID vamos a buscar)
        // - Objetos que simulan respuestas de SWAPI
        // - Configuración de los mocks (qué deben devolver)

        // 1. Definimos el UID que vamos a buscar
        // Este es nuestro "dato de prueba": vamos a buscar el personaje con UID "1"
        String uid = "1";

        // 2. Creamos un DTO simulado que vendría de SWAPI
        //
        // ¿QUÉ ES UN DTO?
        // DTO = Data Transfer Object (Objeto de Transferencia de Datos)
        // Es la estructura de datos que SWAPI devuelve cuando consultamos su API.
        //
        // Este objeto simula lo que SWAPI devolvería si llamáramos al API real.
        // Como estamos usando mocks, no llamamos al API real,
        // pero necesitamos simular cómo se vería la respuesta.
        SwapiPeopleDTO swapiDTO = SwapiPeopleDTO.builder()
                .uid("1")
                .name("Luke Skywalker")
                .height("172")
                .mass("77")
                .hairColor("blond")
                .skinColor("fair")
                .eyeColor("blue")
                .birthYear("19BBY")
                .gender("male")
                .homeworld("https://swapi.tech/api/planets/1")
                .url("https://swapi.tech/api/people/1")
                .build();

        // 3. Creamos el objeto People que esperamos recibir después del mapeo
        //
        // ¿POR QUÉ CREAMOS ESTO?
        // PeopleService convierte el DTO de SWAPI (SwapiPeopleDTO) en nuestro modelo (People).
        // Necesitamos saber cómo se ve el resultado después de esa conversión,
        // para poder verificar que la conversión funcionó correctamente.
        People expectedPeople = People.builder()
                .uid("1")
                .name("Luke Skywalker")
                .height("172")
                .mass("77")
                .hairColor("blond")
                .skinColor("fair")
                .eyeColor("blue")
                .birthYear("19BBY")
                .gender("male")
                .homeworld("https://swapi.tech/api/planets/1")
                .url("https://swapi.tech/api/people/1")
                .build();


        // devuelve el swapiDTO que creamos arriba"
        when(swapiClient.fetchById("people", uid, SwapiPeopleDTO.class))
                .thenReturn(swapiDTO);

        // 5. Configuramos el comportamiento del MOCK de swapiMapper
        //
        // El mapper convierte DTOs de SWAPI a nuestros modelos de dominio.
        // Aquí le decimos al mock del mapper:
        // "Cuando te den este DTO, devuelve este People"
        //
        // En el código real, el mapper hace la conversión.
        // Aquí simulamos esa conversión.
        when(swapiMapper.toPeople(swapiDTO))
                .thenReturn(expectedPeople);

        // ========== ACT (EJECUTAR) ==========
        // En esta sección ejecutamos el método que queremos testear.
        // Este es el "momento de verdad": ¿funciona nuestro código?
        //
        // Llamamos al método real de PeopleService.
        // PeopleService usará nuestros mocks (no objetos reales),
        // pero su lógica será la misma que en producción.
        Optional<People> result = peopleService.findByUid(uid);

        // ========== ASSERT (VERIFICAR) ==========
        // En esta sección verificamos que el resultado es el esperado.
        //
        // ¿QUÉ ES AssertJ?
        // AssertJ es una librería que hace las verificaciones más legibles.
        // En lugar de escribir:
        //   assertTrue(result.isPresent());
        // Escribimos:
        //   assertThat(result).isPresent();
        //
        // Es más legible y natural: "verifica que result sea present"
        //
        // ¿QUÉ ES assertThat()?
        // assertThat(objeto) crea un objeto de verificación para ese objeto.
        // Luego puedes encadenar métodos como .isPresent(), .isEqualTo(), etc.
        //
        // Es como decir: "Verifica que este objeto tenga estas características"

        // Verificación 1: El resultado NO está vacío
        //
        // isPresent() verifica que el Optional contiene un valor.
        // Si está vacío (isEmpty()), significa que no encontró el personaje, y eso es un error.
        assertThat(result)
                .isPresent()  // Verifica que Optional contiene un valor (no está vacío)
                .as("Debería encontrar un personaje con UID %s", uid);  // Mensaje si falla

        // Verificación 2: El resultado contiene el objeto correcto
        //
        // hasValue() verifica que el Optional contiene exactamente el valor esperado.
        // Compara todos los campos del objeto People.
        assertThat(result)
                .hasValue(expectedPeople);

        // Verificación 3: Verificación adicional del nombre
        //
        // A veces queremos verificar campos específicos para asegurarnos de que
        // el mapeo funcionó correctamente.
        //
        // result.get() obtiene el valor del Optional (ya sabemos que existe por la verificación anterior)
        // getName() obtiene el nombre del personaje
        // isEqualTo() verifica que el nombre sea exactamente "Luke Skywalker"
        assertThat(result.get().getName())
                .isEqualTo("Luke Skywalker");
    }

    /**
     * TEST 2: findByUid() - Caso cuando no existe
     *
     * Este test verifica que cuando buscamos un personaje por UID
     * y ese personaje NO existe en SWAPI, el método retorna Optional vacío.
     *
     * ¿POR QUÉ ES IMPORTANTE TESTEAR ESTO?
     * No solo debemos testear el "caso feliz" (cuando todo funciona).
     * También debemos testear casos de error o límite:
     * - ¿Qué pasa si no existe?
     * - ¿Qué pasa si hay un error?
     * - ¿Qué pasa con valores nulos?
     *
     * Esto nos asegura que nuestro código maneja correctamente todos los casos.
     */
    @Test
    @DisplayName("Debería retornar Optional vacío cuando el personaje no existe en SWAPI")
    void testFindByUid_WhenNotExists_ShouldReturnEmpty() {

        // ========== ARRANGE ==========
        // Definimos un UID que no existe en SWAPI
        String uid = "999dsfs";

        // Configuramos el mock para que devuelva null cuando se busque este UID
        //
        // Esto simula que SWAPI no encontró el personaje.
        // En la realidad, si SWAPI no encuentra un personaje, devuelve null o lanza una excepción.
        // Aquí simulamos que devuelve null.
        when(swapiClient.fetchById("people", uid, SwapiPeopleDTO.class))
                .thenReturn(null);  // SWAPI devuelve null cuando no encuentra

        // ========== ACT ==========
        // Ejecutamos el método
        Optional<People> result = peopleService.findByUid(uid);

        // ========== ASSERT ==========
        // Verificamos que el Optional está vacío
        //
        // isEmpty() verifica que el Optional no contiene ningún valor.
        // Esto es lo que esperamos cuando no se encuentra el personaje.
        assertThat(result)
                .isEmpty()  // Verifica que Optional está vacío (no tiene valor)
                .as("Debería retornar Optional vacío cuando el personaje no existe");
    }

    /**
     * TEST 3: findAll() - Con paginación
     *
     * Este test verifica que findAll() puede obtener una lista paginada de personajes
     * desde SWAPI y convertirla correctamente.
     *
     * ¿QUÉ ESTAMOS TESTEANDO?
     * - Que la conversión de paginación funciona (Spring usa 0-based, SWAPI usa 1-based)
     * - Que el mapeo de múltiples DTOs funciona correctamente
     * - Que la construcción de Page de Spring funciona
     */
    @Test
    @DisplayName("Debería retornar lista paginada de personajes desde SWAPI")
    void testFindAll_WithPagination_ShouldReturnPage() {

        // ========== ARRANGE ==========

        // 1. Creamos la paginación de Spring
        // PageRequest.of(página, tamaño)
        // - Página 0: primera página (Spring usa 0-based: 0, 1, 2, ...)
        // - Tamaño 10: 10 elementos por página
        Pageable pageable = PageRequest.of(0, 10);

        // 2. Creamos una lista de DTOs simulados que vendrían de SWAPI
        //
        // List.of() crea una lista inmutable con estos elementos.
        // En un caso real, SWAPI devolvería varios personajes en una página.
        // Aquí simulamos que devuelve 2 personajes.
        List<SwapiPeopleDTO> swapiDTOs = List.of(
                SwapiPeopleDTO.builder()
                        .uid("1")
                        .name("Luke Skywalker")
                        .height("172")
                        .build(),
                SwapiPeopleDTO.builder()
                        .uid("2")
                        .name("Leia Organa")
                        .height("150")
                        .build()
        );

        // 3. Creamos la respuesta paginada simulada de SWAPI
        //
        // SwapiPageResponse es el objeto que SWAPI devuelve cuando pedimos una página.
        // Contiene:
        // - message: estado de la respuesta ("ok", "error", etc.)
        // - totalRecords: total de personajes en SWAPI (82 en este ejemplo)
        // - totalPages: total de páginas (9 páginas si hay 82 personajes y 10 por página)
        // - results: lista de personajes de esta página
        SwapiPageResponse<SwapiPeopleDTO> swapiResponse = SwapiPageResponse.<SwapiPeopleDTO>builder()
                .message("ok")
                .totalRecords(82)  // Total de personajes en SWAPI
                .totalPages(9)      // Total de páginas (82 / 10 = 8.2, redondeado a 9)
                .results(swapiDTOs)  // Los resultados de esta página
                .build();

        // 4. Configuramos el mock: cuando se llame a fetchPage con página 1, devuelve nuestra respuesta
        //
        // IMPORTANTE: Spring usa páginas 0-based (0, 1, 2, ...)
        // Pero SWAPI usa páginas 1-based (1, 2, 3, ...)
        //
        // Por eso en PeopleService hacemos: pageable.getPageNumber() + 1
        // Si Spring pide página 0, SWAPI recibe página 1.
        //
        // En este test, pageable es página 0, entonces PeopleService llamará a SWAPI con página 1.
        when(swapiClient.fetchPage("people", 1, 10, SwapiPeopleDTO.class))
                .thenReturn(swapiResponse);

        // dejar --------------------------------------------------------------

        // any(SwapiPeopleDTO.class) significa "cualquier objeto de tipo SwapiPeopleDTO"
        // No importa cuál DTO específico, cualquier DTO del tipo correcto

        // thenAnswer() permite crear el objeto dinámicamente
        // En lugar de devolver siempre lo mismo, se basa en el argumento recibido
        //
        // invocation.getArgument(0) obtiene el primer argumento (el DTO que se le pasó).
        // Entonces creamos un People basándonos en ese DTO.

        when(swapiMapper.toPeople(any(SwapiPeopleDTO.class)))
                .thenAnswer(invocation -> {
                    // Obtener el DTO que se pasó como argumento
                    SwapiPeopleDTO dto = invocation.getArgument(0);
                    return People.builder()
                            .uid(dto.getUid())
                            .name(dto.getName())
                            .height(dto.getHeight())
                            .build();
                });

        // ========== ACT ==========
        // Ejecutamos el método que queremos testear
        Page<People> result = peopleService.findAll(pageable);

        // ========== ASSERT ==========

        // Verificación 1: La página tiene el tamaño correcto
        // getContent() obtiene la lista de personajes en esta página
        // hasSize(2) verifica que hay exactamente 2 personajes
        assertThat(result.getContent())
                .hasSize(2);

        // Verificación 2: El total de elementos es correcto
        // getTotalElements() obtiene el total de personajes en SWAPI (no solo en esta página)
        // isEqualTo(82) verifica que el total es 82
        assertThat(result.getTotalElements())
                .isEqualTo(82);

        // Verificación 3: Es la primera página
        // isFirst() verifica que esta es la primera página
        assertThat(result.isFirst())
                .isTrue();

        // Verificación 4: Verificar que los nombres están correctos
        // extracting(People::getName) extrae solo el campo "name" de cada personaje
        // containsExactly() verifica que la lista contiene exactamente estos nombres, en este orden
        assertThat(result.getContent())
                .extracting(People::getName)
                .containsExactly("Luke Skywalker", "Leia Organa");
    }

    /**
     * TEST 4: findByNameContaining() - Búsqueda exitosa
     *
     * Este test verifica que findByNameContaining() puede:
     * 1. Obtener personajes de SWAPI
     * 2. Filtrarlos por nombre (búsqueda parcial, case-insensitive)
     * 3. Paginar los resultados filtrados
     *
     * ¿QUÉ ESTAMOS TESTEANDO?
     * - Que el filtrado por nombre funciona correctamente
     * - Que la paginación de resultados filtrados funciona
     * - Que la búsqueda es case-insensitive (no importa mayúsculas/minúsculas)
     */
    @Test
    @DisplayName("Debería filtrar personajes por nombre y paginar resultados")
    void testFindByNameContaining_ShouldFilterAndPaginate() {

        // ========== ARRANGE ==========
        // Definimos el nombre a buscar
        String searchName = "Luke";
        // Creamos paginación: página 0, tamaño 10
        Pageable pageable = PageRequest.of(0, 10);

        // Simulamos que SWAPI devuelve varios personajes
        // Algunos contienen "Luke" en el nombre, otros no.
        List<SwapiPeopleDTO> swapiDTOs = List.of(
                SwapiPeopleDTO.builder().uid("1").name("Luke Skywalker").build(),
                SwapiPeopleDTO.builder().uid("2").name("Leia Organa").build(),
                SwapiPeopleDTO.builder().uid("3").name("Anakin Skywalker").build()
        );

        // Creamos la respuesta de SWAPI simulada
        SwapiPageResponse<SwapiPeopleDTO> swapiResponse = SwapiPageResponse.<SwapiPeopleDTO>builder()
                .results(swapiDTOs)
                .totalRecords(3)
                .build();

        // Configuramos el mock: cuando se llame a fetchPage, devuelve nuestra respuesta
        // NOTA: findByNameContaining busca en la primera página con límite 100
        when(swapiClient.fetchPage("people", 1, 100, SwapiPeopleDTO.class))
                .thenReturn(swapiResponse);

        // Configuramos el mapper para convertir cada DTO
        when(swapiMapper.toPeople(any(SwapiPeopleDTO.class)))
                .thenAnswer(invocation -> {
                    SwapiPeopleDTO dto = invocation.getArgument(0);
                    return People.builder()
                            .uid(dto.getUid())
                            .name(dto.getName())
                            .build();
                });

        // ========== ACT ==========
        // Ejecutamos el método que busca por nombre
        // Este método debería:
        // 1. Obtener todos los personajes de SWAPI (simulados)
        // 2. Filtrarlos por nombre que contenga "Luke"
        // 3. Paginar los resultados
        Page<People> result = peopleService.findByNameContaining(searchName, pageable);

        // ========== ASSERT ==========

        // Verificación 1: Solo encuentra personajes con "Luke" en el nombre
        // 
        // La búsqueda busca que el nombre CONTENGA la cadena de búsqueda (case-insensitive).
        // 
        // De nuestros personajes de prueba:
        // - "Luke Skywalker" contiene "Luke" ✓ (se encuentra)
        // - "Leia Organa" NO contiene "Luke" ✗ (no se encuentra)
        // - "Anakin Skywalker" NO contiene "Luke" ✗ (no se encuentra)
        // 
        // Entonces esperamos solo 1 resultado: "Luke Skywalker"
        assertThat(result.getContent())
                .hasSize(1)  // Solo debería encontrar "Luke Skywalker"
                .extracting(People::getName)  // Extraemos solo los nombres para verificar
                .containsExactly("Luke Skywalker");  // Debe contener exactamente este nombre

        // Verificación 2: Verificar el total de elementos filtrados
        // Después del filtrado, debería haber 1 elemento que coincide
        assertThat(result.getTotalElements())
                .isEqualTo(1);

        // Verificación 3: Verificar que es la primera página
        assertThat(result.isFirst())
                .isTrue();
    }


}
