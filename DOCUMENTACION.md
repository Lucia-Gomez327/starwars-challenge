# Documentación Técnica - Star Wars Challenge API

## Tabla de Contenidos

1. [Arquitectura del Sistema](#arquitectura-del-sistema)
2. [Arquitectura Hexagonal](#arquitectura-hexagonal)
3. [Capa de Dominio](#capa-de-dominio)
4. [Capa de Aplicación](#capa-de-aplicación)
5. [Capa de Infraestructura](#capa-de-infraestructura)
6. [Flujos de Datos](#flujos-de-datos)
7. [Servicios y Funcionalidad](#servicios-y-funcionalidad)
8. [Endpoints REST](#endpoints-rest)
9. [Modelos de Datos](#modelos-de-datos)
10. [Configuración](#configuración)
11. [Testing](#testing)
12. [Convenciones y Estructura](#convenciones-y-estructura)

---

## Arquitectura del Sistema

El proyecto sigue la **Arquitectura Hexagonal (Ports & Adapters)**, también conocida como **Clean Architecture**. Esta arquitectura separa la lógica de negocio de los detalles de implementación, permitiendo que el sistema sea más mantenible, testeable e independiente de frameworks.

### Principios Fundamentales

- **Separación de Responsabilidades**: Cada capa tiene una responsabilidad específica
- **Independencia de Frameworks**: El dominio no depende de Spring Boot ni otras librerías externas
- **Dependencia Invertida**: Las dependencias apuntan hacia el dominio, no desde él
- **Interfaces en el Dominio**: El dominio define interfaces (puertos) que implementa la infraestructura

---

## Arquitectura Hexagonal

### Diagrama de Capas

```
┌─────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE                         │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐ │
│  │ Controllers │  │  SwapiClient │  │  Repositories  │ │
│  │    (in)     │  │    (out)     │  │     (out)      │ │
│  └──────┬──────┘  └──────┬───────┘  └────────┬───────┘ │
└────────┼─────────────────┼─────────────────────┼────────┘
         │                 │                     │
         │                 │                     │
┌────────┼─────────────────┼─────────────────────┼────────┐
│        │                 │                     │         │
│  ┌─────▼─────┐    ┌─────▼──────┐    ┌────────▼──────┐ │
│  │  UseCases │    │  Ports     │    │   Ports        │ │
│  │  (in)     │    │   (in)     │    │    (out)       │ │
│  └───────────┘    └────────────┘    └───────────────┘ │
│                                                           │
│                    APPLICATION                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Services   │  │    Mappers   │  │     DTOs     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└───────────────────────────────────────────────────────────┘
         │
         │
┌────────┼───────────────────────────────────────────────┐
│        │                                               │
│  ┌─────▼──────────┐  ┌──────────────┐  ┌───────────┐ │
│  │   Domain       │  │   Exceptions │  │   Models  │ │
│  │   Models       │  │              │  │           │ │
│  └───────────────┘  └──────────────┘  └───────────┘ │
│                                                         │
│                    DOMAIN                               │
└─────────────────────────────────────────────────────────┘
```

### Descripción de Capas

#### 1. Capa de Dominio (Núcleo)
- **Ubicación**: `com.starwars.domain`
- **Responsabilidad**: Contiene la lógica de negocio pura, independiente de frameworks
- **Componentes**:
  - `model/`: Entidades de dominio
  - `port/in/`: Interfaces de casos de uso (puertos de entrada)
  - `port/out/`: Interfaces de repositorios y servicios externos (puertos de salida)
  - `exception/`: Excepciones de dominio

#### 2. Capa de Aplicación
- **Ubicación**: `com.starwars.application`
- **Responsabilidad**: Orquesta los casos de uso y coordina entre dominio e infraestructura
- **Componentes**:
  - `service/`: Implementación de casos de uso
  - `dto/`: Objetos de transferencia de datos (request/response)
  - `mapper/`: Conversión entre modelos de dominio y DTOs

#### 3. Capa de Infraestructura
- **Ubicación**: `com.starwars.infrastructure`
- **Responsabilidad**: Implementa los detalles técnicos y frameworks
- **Componentes**:
  - `adapter/in/`: Controladores REST (adaptadores de entrada)
  - `adapter/out/`: Cliente SWAPI, Repositorios JPA (adaptadores de salida)
  - `config/`: Configuraciones de Spring

---

## Capa de Dominio

### Modelos de Dominio

#### People (Personaje)
Representa un personaje de Star Wars.

**Campos principales:**
- `uid`: Identificador único en SWAPI
- `name`: Nombre del personaje
- `height`, `mass`: Características físicas
- `hairColor`, `skinColor`, `eyeColor`: Características físicas
- `birthYear`: Año de nacimiento
- `gender`: Género
- `homeworld`: Planeta de origen

#### Film (Película)
Representa una película de Star Wars.

**Campos principales:**
- `uid`: Identificador único en SWAPI
- `title`: Título de la película
- `episodeId`: Número de episodio
- `openingCrawl`: Texto de apertura
- `director`: Director
- `producer`: Productor
- `releaseDate`: Fecha de estreno

#### Starship (Nave Espacial)
Representa una nave espacial de Star Wars.

**Campos principales:**
- `uid`: Identificador único en SWAPI
- `name`: Nombre de la nave
- `model`: Modelo de la nave
- `manufacturer`: Fabricante
- `costInCredits`: Costo en créditos
- `length`, `crew`, `passengers`: Especificaciones técnicas
- `starshipClass`: Clase de nave

#### Vehicle (Vehículo)
Representa un vehículo de Star Wars.

**Campos principales:**
- `uid`: Identificador único en SWAPI
- `name`: Nombre del vehículo
- `model`: Modelo del vehículo
- `manufacturer`: Fabricante
- `costInCredits`: Costo en créditos
- `length`, `crew`, `passengers`: Especificaciones técnicas
- `vehicleClass`: Clase de vehículo

#### User (Usuario)
Representa un usuario del sistema.

**Campos principales:**
- `username`: Nombre de usuario único
- `email`: Email único
- `password`: Contraseña encriptada
- `enabled`: Estado de la cuenta
- `createdAt`: Fecha de creación

### Puertos de Entrada (Use Cases)

Las interfaces en `domain/port/in/` definen los casos de uso del sistema:

#### AuthUseCase
- `register(String username, String password, String email)`: Registra un nuevo usuario
- `login(String username, String password)`: Autentica un usuario y genera token JWT
- `findByUsername(String username)`: Busca un usuario por nombre
- `validateToken(String token)`: Valida un token JWT

#### PeopleUseCase
- `findAll(Pageable pageable)`: Obtiene todos los personajes paginados
- `findByUid(String uid)`: Busca un personaje por ID
- `findByNameContaining(String name, Pageable pageable)`: Busca personajes por nombre

#### FilmUseCase
- `findAll(Pageable pageable)`: Obtiene todas las películas paginadas
- `findByUid(String uid)`: Busca una película por ID
- `findByTitleContaining(String title, Pageable pageable)`: Busca películas por título

#### StarshipUseCase / VehicleUseCase
- Operaciones similares a PeopleUseCase y FilmUseCase

### Puertos de Salida

Las interfaces en `domain/port/out/` definen cómo el dominio interactúa con el exterior:

#### SwapiClient
Interface para comunicarse con la API externa SWAPI:
- `fetchAll(String endpoint, Class<T> type)`: Obtiene todos los recursos
- `fetchById(String endpoint, String id, Class<T> type)`: Obtiene un recurso por ID
- `fetchPage(String endpoint, int page, int limit, Class<T> type)`: Obtiene una página de recursos
- `fetchByName(String endpoint, String name, Class<T> type)`: Busca recursos por nombre
- `fetchByModel(String endpoint, String model, Class<T> type)`: Busca recursos por modelo

#### UserRepository
Interface para persistencia de usuarios:
- `save(User user)`: Guarda un usuario
- `findByUsername(String username)`: Busca usuario por nombre
- `existsByUsername(String username)`: Verifica si existe un usuario
- `existsByEmail(String email)`: Verifica si existe un email

### Excepciones de Dominio

#### DomainException
Excepción base para todas las excepciones de dominio.

#### ResourceNotFoundException
Se lanza cuando no se encuentra un recurso solicitado.

#### AuthenticationException
Se lanza cuando hay errores de autenticación (credenciales inválidas, usuario no encontrado, etc.).

---

## Capa de Aplicación

### Servicios de Aplicación

Los servicios en `application/service/` implementan los casos de uso definidos en los puertos de entrada.

#### AuthService
Implementa `AuthUseCase`.

**Responsabilidades:**
- Validar que el username y email sean únicos al registrar
- Encriptar contraseñas usando BCrypt
- Generar tokens JWT al autenticar
- Validar credenciales de usuario

**Flujo de Registro:**
1. Valida que username y email no existan
2. Encripta la contraseña
3. Crea el usuario en la base de datos
4. Retorna el usuario creado

**Flujo de Login:**
1. Busca el usuario por username
2. Verifica que la contraseña coincida
3. Verifica que la cuenta esté habilitada
4. Genera y retorna un token JWT

#### PeopleService
Implementa `PeopleUseCase`.

**Responsabilidades:**
- Consultar datos desde SWAPI
- Convertir DTOs de SWAPI a modelos de dominio
- Manejar paginación (SWAPI usa página 1-based, Spring usa 0-based)
- Implementar búsqueda por nombre con paginación manual

**Flujo de findAll:**
1. Convierte paginación Spring (0-based) a SWAPI (1-based)
2. Consulta SWAPI con `fetchPage`
3. Mapea DTOs de SWAPI a modelos de dominio
4. Retorna `Page<People>`

#### FilmService
Implementa `FilmUseCase`.

**Responsabilidades:**
- Consultar todas las películas desde SWAPI (SWAPI no tiene paginación para films)
- Implementar paginación en memoria
- Búsqueda por título con filtrado y paginación en memoria

**Flujo de findAll:**
1. Obtiene todas las películas desde SWAPI con `fetchAll`
2. Aplica paginación en memoria
3. Retorna `Page<Film>`

#### StarshipService / VehicleService
Implementan `StarshipUseCase` y `VehicleUseCase` respectivamente, con flujos similares a `PeopleService`.

### Mappers

Los mappers en `application/mapper/` convierten entre modelos de dominio y DTOs de respuesta:

- `PeopleMapper`: Convierte `People` → `PeopleResponse`
- `FilmMapper`: Convierte `Film` → `FilmResponse`
- `StarshipMapper`: Convierte `Starship` → `StarshipResponse`
- `VehicleMapper`: Convierte `Vehicle` → `VehicleResponse`

### DTOs

#### Request DTOs
- `RegisterRequest`: `username`, `password`, `email`
- `LoginRequest`: `username`, `password`

#### Response DTOs
- `AuthResponse`: `token`, `type`, `username`, `email`
- `PeopleResponse`: Todos los campos de `People`
- `FilmResponse`: Todos los campos de `Film`
- `StarshipResponse`: Todos los campos de `Starship`
- `VehicleResponse`: Todos los campos de `Vehicle`
- `PageResponse<T>`: Respuesta paginada con `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`, `first`

---

## Capa de Infraestructura

### Adaptadores de Entrada (Controllers)

Los controladores en `infrastructure/adapter/in/rest/` exponen la API REST.

#### AuthController
**Endpoints:**
- `POST /api/v1/auth/register`: Registra un nuevo usuario
  - Body: `RegisterRequest`
  - Response: `AuthResponse` (201 Created)
  - Flujo: Registra usuario y automáticamente genera token

- `POST /api/v1/auth/login`: Autentica un usuario
  - Body: `LoginRequest`
  - Response: `AuthResponse` (200 OK)

#### PeopleController
**Endpoints:**
- `GET /api/v1/people?page={page}&size={size}`: Obtiene personajes paginados
  - Parámetros: `page` (0-based, default: 0), `size` (default: 10)
  - Response: `PageResponse<PeopleResponse>`
  - Requiere: Token JWT

- `GET /api/v1/people/search?id={id}`: Busca personaje por ID
  - Response: `PeopleResponse`

- `GET /api/v1/people/search?name={name}`: Busca personajes por nombre
  - Response: `List<PeopleResponse>`

#### FilmController
**Endpoints:**
- `GET /api/v1/films?page={page}&size={size}`: Obtiene películas paginadas
  - Parámetros: `page` (1-based, default: 1), `size` (default: 10)
  - Response: `PageResponse<FilmResponse>`

- `GET /api/v1/films/search?id={id}`: Busca película por ID
  - Response: `FilmResponse`

- `GET /api/v1/films/search?title={title}&page={page}&size={size}`: Busca películas por título
  - Response: `PageResponse<FilmResponse>`

#### StarshipController / VehicleController
Endpoints similares a `PeopleController` y `FilmController`, con búsqueda adicional por `model`.

### Adaptadores de Salida

#### SwapiClientImpl
Implementa `SwapiClient` usando `RestTemplate`.

**Responsabilidades:**
- Construir URLs para SWAPI
- Hacer peticiones HTTP a SWAPI
- Manejar diferentes formatos de respuesta de SWAPI
- Convertir respuestas JSON a objetos Java

**Manejo de Respuestas:**
- SWAPI puede devolver datos en formato `result` (array plano) o `results` (paginado)
- Algunos recursos anidan datos en `properties`
- Maneja conversión de tipos y mapeo de campos

#### SwapiMapper
Convierte DTOs de SWAPI a modelos de dominio:
- `toPeople(SwapiPeopleDTO)`: Convierte a `People`
- `toFilm(SwapiFilmDTO)`: Convierte a `Film`
- `toStarship(SwapiStarshipDTO)`: Convierte a `Starship`
- `toVehicle(SwapiVehicleDTO)`: Convierte a `Vehicle`

#### UserRepositoryAdapter
Implementa `UserRepository` usando Spring Data JPA.

### Configuraciones

#### SecurityConfig
Configura Spring Security:
- Desactiva CSRF para API REST
- Permite acceso público a `/api/v1/auth/**`, Swagger y H2 Console
- Protege todos los demás endpoints con JWT
- Configura `JwtAuthenticationFilter` para validar tokens

#### SwaggerConfig
Configura OpenAPI/Swagger:
- Define información de la API
- Configura esquema de seguridad Bearer JWT

#### RestTemplateConfig
Configura `RestTemplate` para llamadas HTTP a SWAPI.

---

## Flujos de Datos

### Flujo de Autenticación

```
Cliente → AuthController → AuthService → UserRepository → Base de Datos
                     ↓
              JwtTokenProvider → Token JWT
                     ↓
              AuthResponse → Cliente
```

### Flujo de Consulta de Recursos

```
Cliente → Controller → UseCase (Service) → SwapiClient → SWAPI API
                     ↓                    ↓
                Mapper               SwapiMapper
                     ↓                    ↓
              DTO Response ← Domain Model ← SWAPI DTO
                     ↓
                  Cliente
```

### Flujo de Búsqueda con Paginación

```
1. Cliente solicita GET /api/v1/people?page=0&size=10
2. PeopleController valida token JWT
3. PeopleController llama a PeopleUseCase.findAll(pageable)
4. PeopleService convierte página 0-based a 1-based
5. PeopleService llama a SwapiClient.fetchPage("people", 1, 10)
6. SwapiClientImpl hace petición HTTP a SWAPI
7. SWAPI retorna datos paginados
8. SwapiMapper convierte DTOs a modelos de dominio
9. PeopleMapper convierte modelos a DTOs de respuesta
10. Cliente recibe PageResponse<PeopleResponse>
```

---

## Servicios y Funcionalidad

### AuthService
**Funcionalidad principal:**
- Gestión de usuarios (registro, autenticación)
- Generación y validación de tokens JWT
- Encriptación de contraseñas con BCrypt

**Dependencias:**
- `UserRepository`: Persistencia de usuarios
- `PasswordEncoder`: Encriptación de contraseñas
- `JwtTokenProvider`: Generación de tokens

### PeopleService
**Funcionalidad principal:**
- Consulta de personajes desde SWAPI
- Paginación y búsqueda
- Conversión de formatos de SWAPI a modelos de dominio

**Características:**
- Maneja diferencia entre paginación 0-based (Spring) y 1-based (SWAPI)
- Implementa búsqueda por nombre con paginación manual (SWAPI no soporta búsqueda paginada)

### FilmService
**Funcionalidad principal:**
- Consulta de películas desde SWAPI
- Paginación en memoria (SWAPI no tiene paginación para films)
- Búsqueda por título con filtrado y paginación

**Características:**
- Obtiene todas las películas y aplica paginación en memoria
- Filtrado por título con búsqueda case-insensitive

### StarshipService / VehicleService
Similar a `PeopleService`, pero también soportan búsqueda por modelo.

---

## Endpoints REST

### Endpoints Públicos (No requieren autenticación)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Registra un nuevo usuario |
| POST | `/api/v1/auth/login` | Autentica un usuario |

### Endpoints Protegidos (Requieren Token JWT)

#### People
| Método | Endpoint | Parámetros | Descripción |
|--------|----------|------------|-------------|
| GET | `/api/v1/people` | `page`, `size` | Lista personajes paginados (0-based) |
| GET | `/api/v1/people/search` | `id` o `name` | Busca personajes |

#### Films
| Método | Endpoint | Parámetros | Descripción |
|--------|----------|------------|-------------|
| GET | `/api/v1/films` | `page`, `size` | Lista películas paginadas (1-based) |
| GET | `/api/v1/films/search` | `id` o `title`, `page`, `size` | Busca películas |

#### Starships
| Método | Endpoint | Parámetros | Descripción |
|--------|----------|------------|-------------|
| GET | `/api/v1/starships` | `page`, `size` | Lista naves paginadas (1-based) |
| GET | `/api/v1/starships/search` | `id`, `name` o `model`, `page`, `size` | Busca naves |

#### Vehicles
| Método | Endpoint | Parámetros | Descripción |
|--------|----------|------------|-------------|
| GET | `/api/v1/vehicles` | `page`, `size` | Lista vehículos paginados (1-based) |
| GET | `/api/v1/vehicles/search` | `id`, `name` o `model`, `page`, `size` | Busca vehículos |

### Códigos de Estado HTTP

- `200 OK`: Petición exitosa
- `201 Created`: Recurso creado exitosamente (registro de usuario)
- `400 Bad Request`: Parámetros inválidos o faltantes
- `401 Unauthorized`: Token JWT inválido o ausente
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error interno del servidor

---

## Modelos de Datos

### Modelos de Dominio

Los modelos de dominio son clases simples (POJOs) con Lombok que representan las entidades del negocio.

**Ubicación**: `com.starwars.domain.model`

**Características:**
- No tienen dependencias de frameworks
- Representan el estado y estructura de las entidades
- Usan `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` de Lombok

### DTOs de Request

**Ubicación**: `com.starwars.application.dto.request`

- `RegisterRequest`: Validación con `@Valid`, `@NotBlank`, `@Email`
- `LoginRequest`: Validación con `@Valid`, `@NotBlank`

### DTOs de Response

**Ubicación**: `com.starwars.application.dto.response`

- DTOs de respuesta reflejan los modelos de dominio pero pueden tener campos adicionales o transformados
- `PageResponse<T>`: Estructura genérica para respuestas paginadas

### DTOs de SWAPI

**Ubicación**: `com.starwars.infrastructure.adapter.out.client.dto`

- `SwapiPeopleDTO`, `SwapiFilmDTO`, `SwapiStarshipDTO`, `SwapiVehicleDTO`
- Representan la estructura exacta de las respuestas de SWAPI
- Se mapean a modelos de dominio mediante `SwapiMapper`

---

## Configuración

### Perfiles de Spring Boot

#### Perfil `h2` (default)
**Archivo**: `application-h2.yml`

- Base de datos H2 en memoria
- JWT secret para desarrollo
- Logging en nivel DEBUG
- H2 Console habilitada

**Uso**: Desarrollo local

#### Perfil `test`
**Archivo**: `application-test.yml`

- Base de datos H2 en memoria
- Configuración optimizada para tests
- Logging mínimo

**Uso**: Ejecución de tests

#### Perfil `prod`
**Archivo**: `application-prod.yml`

- Usa variables de entorno para configuración sensible
- Logging en nivel INFO
- Puerto configurable mediante variable `PORT`

**Variables de entorno requeridas:**
- `JWT_SECRET`: Secreto para firmar tokens JWT
- `JWT_EXPIRATION`: Duración del token en milisegundos
- `SWAPI_BASE_URL`: URL base de SWAPI
- `PORT`: Puerto de la aplicación

### Configuración de Seguridad

**JWT:**
- Algoritmo: HS256
- Secreto: Configurable por perfil
- Expiración: 24 horas por defecto (86400000 ms)

**Spring Security:**
- CSRF deshabilitado (API REST stateless)
- Endpoints públicos: `/api/v1/auth/**`, Swagger UI, H2 Console
- Resto de endpoints protegidos con JWT

### Configuración de SWAPI

**URL Base**: `https://www.swapi.tech/api`

**Endpoints soportados:**
- `/people`
- `/films`
- `/starships`
- `/vehicles`

---

## Testing

### Tipos de Tests

#### Tests Unitarios

**Ubicación**: `src/test/java/com/starwars/application/service/`

**Estrategia:**
- Mockean dependencias externas (SwapiClient, Repositories)
- Testean lógica de negocio aislada
- Verifican transformaciones de datos y paginación

**Ejemplos:**
- `PeopleServiceTest`: Verifica consultas a SWAPI, paginación, búsqueda
- `FilmServiceTest`: Verifica paginación en memoria, búsqueda por título
- `AuthServiceTest`: Verifica registro, login, validación de credenciales

#### Tests de Integración

**Ubicación**: `src/test/java/com/starwars/infrastructure/adapter/in/`

**Estrategia:**
- Usan `@SpringBootTest` para levantar contexto completo
- Mockean solo SWAPI (evita llamadas externas reales)
- Verifican flujo completo desde controlador hasta servicio

**Ejemplos:**
- `PeopleControllerIntegrationTest`: Verifica flujo completo de consulta de personajes

#### Tests de Seguridad

**Ubicación**: `src/test/java/com/starwars/infrastructure/adapter/in/`

**Estrategia:**
- Usan `@WithMockUser` o configuran tokens JWT
- Verifican que endpoints protegidos requieran autenticación
- Verifican que endpoints públicos sean accesibles

**Ejemplos:**
- `PeopleControllerSecurityTest`: Verifica protección de endpoints
- `FilmControllerSecurityTest`: Similar para películas

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Solo tests unitarios
mvn test -Dtest=*ServiceTest

# Solo tests de integración
mvn test -Dtest=*IntegrationTest

# Solo tests de seguridad
mvn test -Dtest=*SecurityTest
```

### Cobertura de Tests

Los tests cubren:
- ✅ Servicios de aplicación (lógica de negocio)
- ✅ Controladores (endpoints REST)
- ✅ Seguridad (autenticación y autorización)
- ✅ Flujos de integración

---

## Convenciones y Estructura

### Estructura de Paquetes

```
com.starwars
├── domain
│   ├── model              # Entidades de dominio
│   ├── port
│   │   ├── in             # Interfaces de casos de uso
│   │   └── out            # Interfaces de repositorios y servicios externos
│   └── exception          # Excepciones de dominio
├── application
│   ├── service            # Implementación de casos de uso
│   ├── dto
│   │   ├── request        # DTOs de petición
│   │   └── response       # DTOs de respuesta
│   └── mapper             # Mappers dominio ↔ DTOs
└── infrastructure
    ├── adapter
    │   ├── in
    │   │   ├── rest       # Controladores REST
    │   │   └── security   # Configuración de seguridad
    │   └── out
    │       ├── client     # Cliente SWAPI
    │       └── persistence # Repositorios JPA
    └── config             # Configuraciones de Spring
```

### Convenciones de Nombrado

- **Interfaces de casos de uso**: `*UseCase` (ej: `PeopleUseCase`)
- **Servicios**: `*Service` (ej: `PeopleService`)
- **Controladores**: `*Controller` (ej: `PeopleController`)
- **Mappers**: `*Mapper` (ej: `PeopleMapper`)
- **DTOs**: `*Request`, `*Response` (ej: `RegisterRequest`, `PeopleResponse`)
- **Repositorios**: `*Repository` (ej: `UserRepository`)
- **Excepciones**: `*Exception` (ej: `ResourceNotFoundException`)

### Principios SOLID Aplicados

- **S**ingle Responsibility: Cada clase tiene una responsabilidad única
- **O**pen/Closed: Extensiones mediante interfaces sin modificar código existente
- **L**iskov Substitution: Interfaces bien definidas
- **I**nterface Segregation: Interfaces específicas y pequeñas
- **D**ependency Inversion: Dependencias hacia abstracciones (interfaces)

---

## Flujo Completo de una Petición

### Ejemplo: Obtener Personajes

1. **Cliente** envía: `GET /api/v1/people?page=0&size=10` con header `Authorization: Bearer <token>`

2. **SecurityConfig** valida el token JWT usando `JwtAuthenticationFilter`

3. **PeopleController** recibe la petición y valida parámetros

4. **PeopleController** llama a `PeopleUseCase.findAll(pageable)`

5. **PeopleService** (implementa `PeopleUseCase`):
   - Convierte paginación 0-based a 1-based
   - Llama a `SwapiClient.fetchPage("people", 1, 10)`

6. **SwapiClientImpl** construye URL y hace petición HTTP a SWAPI

7. **SWAPI** retorna datos paginados en formato JSON

8. **SwapiClientImpl** parsea JSON a `SwapiPeopleDTO`

9. **SwapiMapper** convierte `SwapiPeopleDTO[]` a `People[]`

10. **PeopleService** crea `Page<People>` con los datos

11. **PeopleMapper** convierte `People[]` a `PeopleResponse[]`

12. **PeopleController** construye `PageResponse<PeopleResponse>`

13. **Cliente** recibe respuesta JSON con personajes paginados

---

## Consideraciones Técnicas

### Paginación

**People, Starships, Vehicles:**
- SWAPI soporta paginación nativa
- La aplicación convierte entre 0-based (Spring) y 1-based (SWAPI)

**Films:**
- SWAPI no soporta paginación para films
- La aplicación obtiene todos los films y aplica paginación en memoria

### Búsqueda

**Por nombre/título:**
- SWAPI soporta búsqueda pero no con paginación
- La aplicación obtiene todos los resultados y aplica paginación manual

**Por modelo:**
- Solo disponible para Starships y Vehicles
- Similar a búsqueda por nombre

### Manejo de Errores

- **Excepciones de dominio**: Se propagan hasta el controlador
- **Controladores**: Capturan excepciones y retornan códigos HTTP apropiados
- **SWAPI no disponible**: Se retorna lista vacía o error 500 según el caso

### Performance

- **Cache**: No implementado actualmente (consultas siempre a SWAPI)
- **Optimización futura**: Podría implementarse cache de resultados de SWAPI
- **Paginación**: Minimiza transferencia de datos innecesarios


## Referencias

- **SWAPI Documentation**: https://swapi.tech/
- **Spring Boot Documentation**: https://spring.io/projects/spring-boot

