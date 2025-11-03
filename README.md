# Star Wars Challenge API

API REST desarrollada con Java 21 y Spring Boot que integra la API de Star Wars (SWAPI) con autenticación JWT y arquitectura hexagonal.

##  Tecnologías

- **Java 21** (Temurin/OpenJDK)
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security** + JWT
- **H2 Database** (en memoria, por defecto)
- **Maven**
- **Lombok**
- **Swagger/OpenAPI**
- **JUnit 5**, **Mockito**, **AssertJ** (Testing)

##  Requisitos Previos

- **Java 21** o superior
- **Maven 3.8+**
- **Git** (para clonar el repositorio)

## 🏗 Arquitectura

El proyecto sigue **Arquitectura Hexagonal (Ports & Adapters)**:

```
src/main/java/com/starwars/
├── domain/          # Capa de Dominio (núcleo de negocio)
│   ├── model/       # Entidades de dominio
│   ├── port/        # Interfaces (casos de uso y repositorios)
│   └── exception/   # Excepciones de dominio
├── application/     # Capa de Aplicación
│   ├── service/     # Casos de uso
│   ├── dto/         # DTOs de request/response
│   └── mapper/      # Mappers entre dominio y DTOs
└── infrastructure/  # Capa de Infraestructura
    ├── adapter/
    │   ├── in/      # Controladores REST
    │   └── out/     # Cliente SWAPI y Repositorios JPA
    └── config/      # Configuraciones
```

## ⚙️ Instalación y Ejecución

### Pasos de Instalación

La aplicación usa **H2 en memoria** por defecto, no requiere configuración adicional:

```bash
# 1. Clonar repositorio
git clone https://github.com/Lucia-Gomez327/starwars-challenge.git
cd starwars-challenge

# 2. Compilar y ejecutar
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

**Nota:** Con H2, los datos se almacenan solo en memoria y se pierden al reiniciar la aplicación.


## 📚 Documentación API

### Swagger UI

Una vez que la aplicación esté ejecutándose, accede a la documentación interactiva:

**http://localhost:8080/swagger-ui.html**

Desde aquí puedes:
- Ver todos los endpoints disponibles
- Probar los endpoints directamente
- Ver ejemplos de requests y responses
- Autenticarte con JWT

### Endpoints Principales

#### Autenticación

**POST** `/api/auth/register` - Registro de nuevo usuario

```json
{
  "username": "usuario",
  "password": "password123",
  "email": "usuario@example.com"
}
```

**POST** `/api/auth/login` - Iniciar sesión

```json
{
  "username": "usuario",
  "password": "password123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "usuario",
  "email": "usuario@example.com"
}
```

**Importante:** Copia el token para usarlo en las peticiones protegidas.

### Flujo de Autenticación Completo

#### 1. Registro de Usuario

El proceso de registro crea un nuevo usuario en el sistema:

1. **Enviar petición POST** a `/api/auth/register` con:
    - `username`: Nombre de usuario único
    - `password`: Contraseña del usuario
    - `email`: Email válido y único

2. **El sistema:**
    - Valida que el username y email no existan
    - Encripta la contraseña usando BCrypt
    - Crea el usuario en la base de datos
    - Genera automáticamente un token JWT
    - Retorna el token junto con los datos del usuario

**Ejemplo de respuesta exitosa (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "usuario",
  "email": "usuario@example.com"
}
```

#### 2. Inicio de Sesión

Si ya tienes una cuenta registrada:

1. **Enviar petición POST** a `/api/auth/login` con:
    - `username`: Tu nombre de usuario
    - `password`: Tu contraseña

2. **El sistema:**
    - Valida las credenciales
    - Verifica que la cuenta esté habilitada
    - Genera un token JWT
    - Retorna el token junto con los datos del usuario

**Ejemplo de respuesta exitosa (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "usuario",
  "email": "usuario@example.com"
}
```

#### 3. Uso del Token JWT

Para acceder a los endpoints protegidos, incluye el token en el header `Authorization`:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Importante:**
- El token tiene una duración predeterminada de 24 horas (configurable)
- Si el token expira, deberás iniciar sesión nuevamente
- El token se incluye en todas las peticiones a `/api/people`, `/api/films`, `/api/starships`, `/api/vehicles`

#### People (Personajes)

**GET** `/api/people` - Obtener personajes con paginación:

```
GET /api/people?page=0&size=10
Authorization: Bearer tu-token-jwt-aqui
```

**Parámetros:**
- `page` (opcional, default: 0): Número de página
- `size` (opcional, default: 10): Tamaño de la página

**Respuesta:**
```json
{
  "content": [
    {
      "id": 1,
      "uid": "1",
      "name": "Luke Skywalker",
      "height": "172",
      "mass": "77",
      ...
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 82,
  "totalPages": 9,
  "last": false,
  "first": true
}
```

**GET** `/api/people/search` - Buscar personajes por ID o nombre:

```
GET /api/people/search?id=1
GET /api/people/search?name=Luke
Authorization: Bearer tu-token-jwt-aqui
```

#### Films (Películas)

**GET** `/api/films` - Obtener películas con paginación

```
GET /api/films?page=1&size=10
Authorization: Bearer tu-token-jwt-aqui
```

**GET** `/api/films/search` - Buscar películas por ID o título:

```
GET /api/films/search?id=1
GET /api/films/search?title=A New Hope&page=1&size=10
Authorization: Bearer tu-token-jwt-aqui
```

#### Starships (Naves Espaciales)

**GET** `/api/starships` - Obtener naves con paginación

```
GET /api/starships?page=1&size=10
Authorization: Bearer tu-token-jwt-aqui
```

**GET** `/api/starships/search` - Buscar naves por ID, nombre o modelo:

```
GET /api/starships/search?id=1
GET /api/starships/search?name=Death Star
GET /api/starships/search?model=Star Destroyer&page=1&size=10
Authorization: Bearer tu-token-jwt-aqui
```

#### Vehicles (Vehículos)

**GET** `/api/vehicles` - Obtener vehículos con paginación ):

```
GET /api/vehicles?page=1&size=10
Authorization: Bearer tu-token-jwt-aqui
```

**GET** `/api/vehicles/search` - Buscar vehículos por ID, nombre o modelo:

```
GET /api/vehicles/search?id=1
GET /api/vehicles/search?name=Sand Crawler
GET /api/vehicles/search?model=AT-AT&page=1&size=10
Authorization: Bearer tu-token-jwt-aqui
```

**Nota importante:**
- Todos los endpoints (excepto `/api/auth/**`) requieren autenticación JWT
- Todos los datos se consultan en tiempo real desde SWAPI
- La paginación en Films, Starships y Vehicles es 1-based (la primera página es 1, no 0)

## 🧪 Testing

El proyecto incluye tests unitarios y de integración:

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar solo tests unitarios
mvn test -Dtest=*ServiceTest

# Ejecutar solo tests de seguridad
mvn test -Dtest=*SecurityTest

# Ejecutar solo tests de integración
mvn test -Dtest=*IntegrationTest
```

### Tipos de Tests

- **Tests Unitarios:** Testean servicios individuales con mocks
    - `PeopleServiceTest`, `FilmServiceTest`, `StarshipServiceTest`, `VehicleServiceTest`

- **Tests de Seguridad:** Verifican que los endpoints estén protegidos
    - `PeopleControllerSecurityTest`, `FilmControllerSecurityTest`, etc.

- **Tests de Integración:** Verifican el flujo completo desde el controlador hasta SWAPI
    - `PeopleControllerIntegrationTest`

## 📦 Despliegue

### Railway


## 🔧 Configuración

### Perfiles de Spring Boot

La aplicación tiene tres perfiles configurados:

- **`h2`** (default): Usa H2 en memoria
- **`test`**: Configuración para tests (H2 en memoria)
- **`prod`**: Configuración de producción

### Variables de Entorno

Para ejecutar en producción, configura estas variables de entorno:

#### Variables Requeridas para Producción:

```bash
# Secreto JWT (mínimo 256 bits de seguridad)
JWT_SECRET=tu-secreto-super-seguro-de-al-menos-256-bits-para-jwt-en-produccion

# Duración del token JWT en milisegundos (default: 86400000 = 24 horas)
JWT_EXPIRATION=86400000

# URL base de SWAPI (default: https://www.swapi.tech/api)
SWAPI_BASE_URL=https://www.swapi.tech/api

# Perfil de Spring Boot activo
SPRING_PROFILES_ACTIVE=prod

# Puerto de la aplicación (default: 8080)
PORT=8080
```

#### Configuración en Archivo (Desarrollo)

En desarrollo, estas variables se configuran en `application-h2.yml`:

```yaml
jwt:
  secret: mi-secreto-super-seguro-de-al-menos-256-bits-para-jwt-en-desarrollo
  expiration: 86400000 # 24 horas en milisegundos

swapi:
  base-url: https://www.swapi.tech/api
```

## 📝 Estado del Proyecto

- Configuración base del proyecto
- Arquitectura hexagonal implementada
- Modelos de dominio
- Excepciones de dominio
- Puertos (interfaces) definidos
- DTOs y mappers
- Servicios de aplicación
- Autenticación JWT completa
- Controladores REST con endpoints unificados
- Swagger/OpenAPI configurado
- Cliente SWAPI integrado
- Consulta SWAPI en tiempo real
- Tests unitarios completos
- Tests de seguridad
- Tests de integración

## 🔐 Seguridad

- **Autenticación JWT:** Todos los endpoints (excepto `/api/auth/**`) requieren token JWT
- **Spring Security:** Configurado para proteger las rutas
- **CORS:** Configurado para permitir peticiones desde cualquier origen

## 📞 Uso de la API

### Ejemplo Completo de Uso

#### 1. Registrar Usuario

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser",
  "email": "test@example.com"
}
```

#### 2. Iniciar Sesión

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser",
  "email": "test@example.com"
}
```

#### 3. Obtener Personajes (con token)

Guarda el token de la respuesta anterior en una variable:

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET "http://localhost:8080/api/people?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

#### 4. Buscar Personaje por Nombre

```bash
curl -X GET "http://localhost:8080/api/people/search?name=Luke" \
  -H "Authorization: Bearer $TOKEN"
```

#### 5. Obtener Películas

```bash
curl -X GET "http://localhost:8080/api/films?page=1&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

#### 6. Buscar Nave Espacial

```bash
curl -X GET "http://localhost:8080/api/starships/search?name=Death Star" \
  -H "Authorization: Bearer $TOKEN"
```

### Ejemplos con Postman

1. **Importar colección:** Puedes usar Swagger UI para generar una colección de Postman
2. **Configurar Bearer Token:** En la pestaña Authorization, selecciona "Bearer Token" y pega tu token
3. **Probar endpoints:** Todas las peticiones a `/api/*` (excepto `/api/auth/**`) requerirán el token

### Problemas Comunes

#### Error: "El usuario ya existe"
- **Causa:** Intentas registrar un usuario con un username o email que ya existe
- **Solución:** Usa un username o email diferente, o inicia sesión con las credenciales existentes

#### Error: "Credenciales inválidas"
- **Causa:** Username o password incorrectos
- **Solución:** Verifica que estés usando las credenciales correctas

#### Error: 401 Unauthorized
- **Causa:** Token JWT inválido, expirado o no incluido
- **Solución:**
    - Verifica que incluyas el header `Authorization: Bearer <token>`
    - Asegúrate de que el token no haya expirado (dura 24 horas por defecto)
    - Si el token expiró, inicia sesión nuevamente para obtener un nuevo token

#### Error: 404 Not Found en endpoints de SWAPI
- **Causa:** El recurso solicitado no existe en SWAPI
- **Solución:** Verifica que el ID o nombre del recurso sea correcto

#### La aplicación no inicia
- **Causa:** Puerto 8080 en uso o problemas de configuración
- **Solución:**
    - Cambia el puerto en `application.yml`: `server.port: 8081`
    - Verifica que Java 21 esté instalado: `java -version`
    - Verifica que Maven esté instalado: `mvn -version`

#### Error de conexión a SWAPI
- **Causa:** SWAPI puede estar temporalmente no disponible o problemas de red
- **Solución:**
    - Verifica tu conexión a internet
    - Verifica que la URL de SWAPI sea correcta: `https://www.swapi.tech/api`
    - Intenta nuevamente después de unos minutos


## 👤 Autor

Lucia Gomez - [@Lucia-Gomez327](https://github.com/Lucia-Gomez327)

