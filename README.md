# Star Wars Challenge API

API REST desarrollada con Java 21 y Spring Boot que integra la API de Star Wars (SWAPI) con autenticación JWT y arquitectura hexagonal.

## 🚀 Tecnologías

- **Java 21** (Temurin/OpenJDK)
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security** + JWT
- **H2 Database** (en memoria, por defecto) / **PostgreSQL** (opcional)
- **Maven**
- **Lombok**
- **Swagger/OpenAPI**
- **JUnit 5**, **Mockito**, **AssertJ** (Testing)

## 📋 Requisitos Previos

- **Java 21** o superior
- **Maven 3.8+**
- **Git** (para clonar el repositorio)
- **Docker y Docker Compose** (opcional, solo si quieres usar PostgreSQL)

## 🏗️ Arquitectura

El proyecto sigue **Arquitectura Hexagonal (Ports & Adapters)**:

```
src/main/java/com/starwars/
├── domain/           # Capa de Dominio (núcleo de negocio)
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

### Opción 1: Con H2 (Base de datos en memoria) - Recomendado para desarrollo

La aplicación usa **H2 en memoria** por defecto, no requiere Docker ni configuración adicional:

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

#### People (Personajes)

**GET** `/api/people` - Endpoint unificado con parámetros opcionales:

- **Sin parámetros:** Devuelve todos los personajes
  ```
  GET /api/people
  ```

- **Por ID:** Devuelve un personaje específico
  ```
  GET /api/people?id=1
  ```

- **Por nombre:** Busca personajes que contengan el nombre
  ```
  GET /api/people?name=Luke
  ```

- **Paginado:** Devuelve resultados paginados
  ```
  GET /api/people?page=0&size=10
  ```

- **Combinaciones:** Puedes combinar parámetros
  ```
  GET /api/people?name=Luke&page=0&size=10
  ```

**Headers requeridos para endpoints protegidos:**
```
Authorization: Bearer tu-token-jwt-aqui
```

#### Films, Starships, Vehicles

Endpoints unificados similares para cada entidad:
- **`/api/films`** - Películas de Star Wars
- **`/api/starships`** - Naves espaciales
- **`/api/vehicles`** - Vehículos

Todos tienen el mismo comportamiento que `/api/people` y consultan SWAPI en tiempo real.

**Nota importante:** Todos los endpoints (excepto `/api/auth/**`) requieren autenticación JWT.

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

###  Railway

Railway es la opción más fácil para desplegar aplicaciones Spring Boot:

1. **Crear cuenta** en [Railway](https://railway.app) (usa tu cuenta de GitHub)
2. **Crear nuevo proyecto** → "Deploy from GitHub repo"
3. **Seleccionar** tu repositorio `starwars-challenge`
4. **Agregar base de datos PostgreSQL:**
   - Click en "New" → "Database" → "PostgreSQL"
   - Railway configura automáticamente las variables de entorno
5. **Configurar variables de entorno:**
   - `JWT_SECRET`: Genera un secreto seguro (mínimo 256 bits)
   - `JWT_EXPIRATION`: `86400000` (24 horas)
   - `SWAPI_BASE_URL`: `https://www.swapi.tech/api`
6. **Deploy automático:** Railway despliega automáticamente en cada push a GitHub

Railway ofrece $5 de crédito gratuito al mes, suficiente para proyectos pequeños.

### Alternativa: Render

1. **Crear cuenta** en [Render](https://render.com)
2. **Crear nuevo "Web Service"**
3. **Conectar** tu repositorio de GitHub
4. **Configurar** como Spring Boot o Docker
5. **Agregar base de datos PostgreSQL** gratuita
6. **Configurar variables de entorno** (igual que Railway)

**Nota:** Render tiene plan gratuito pero la app se duerme después de 15 minutos de inactividad.

## 🔧 Configuración

### Perfiles de Spring Boot

La aplicación tiene tres perfiles configurados:

- **`h2`** (default): Usa H2 en memoria, perfecto para desarrollo
- **`dev`**: Usa PostgreSQL con Docker
- **`test`**: Configuración para tests (H2 en memoria)
- **`prod`**: Configuración de producción

### Variables de Entorno

Para producción, configura estas variables:


``

## 📝 Estado del Proyecto

✅ **Completado:**
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

### Ejemplo completo de uso:

1. **Registrar usuario:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com"}'
```

2. **Iniciar sesión:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

3. **Obtener personajes (con token):**
```bash
curl -X GET http://localhost:8080/api/people?page=0&size=10 \
  -H "Authorization: Bearer TU-TOKEN-AQUI"
```

## 👤 Autor

Lucia Gomez - [@Lucia-Gomez327](https://github.com/Lucia-Gomez327)

