# Star Wars Challenge API

API REST desarrollada con Java 21 y Spring Boot que integra la API de Star Wars (SWAPI) con autenticación JWT y arquitectura hexagonal.

## 🚀 Tecnologías

- **Java 21** (Temurin/OpenJDK)
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **Spring Security** + JWT
- **PostgreSQL 15** (Docker)
- **Maven**
- **Lombok**
- **Swagger/OpenAPI**

## 📋 Requisitos Previos

- Java 21 o superior
- Docker y Docker Compose
- Maven 3.8+

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
    │   └── out/     # Repositorios JPA
    └── config/      # Configuraciones
```

## ⚙️ Instalación

### 1. Clonar repositorio

```bash
git clone https://github.com/tu-usuario/starwars-challenge.git
cd starwars-challenge
```

### 2. Levantar PostgreSQL con Docker

```bash
docker-compose up -d
```

### 3. Compilar y ejecutar

```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

## 📚 Documentación API

### Swagger UI

Documentación interactiva: **http://localhost:8080/swagger-ui.html**

### Endpoints Principales

#### Autenticación

**POST** `/api/auth/register`
```json
{
  "username": "usuario",
  "password": "password123",
  "email": "usuario@example.com"
}
```

**POST** `/api/auth/login`
```json
{
  "username": "usuario",
  "password": "password123"
}
```

#### People (Personajes)

**GET** `/api/people` - Endpoint unificado con parámetros opcionales:
- Sin parámetros: devuelve todos los personajes
- Con `id`: devuelve un solo personaje por ID (ej: `?id=1`)
- Con `name`: busca por nombre (ej: `?name=Luke`)
- Con `page` y `size`: devuelve paginado (ej: `?page=0&size=10`)
- Combinaciones: `?name=Luke&page=0&size=10`
- Requiere autenticación JWT

#### Films, Starships, Vehicles

Endpoints unificados similares para cada entidad:
- `/api/films` - Mismo comportamiento que People
- `/api/starships` - Mismo comportamiento que People
- `/api/vehicles` - Mismo comportamiento que People

Todos consultan SWAPI en tiempo real, sin almacenamiento local.

## 🧪 Testing

```bash
mvn test
```

## 📦 Despliegue

### Docker

```bash
docker build -t starwars-challenge .
docker run -p 8080:8080 starwars-challenge
```

## 📝 Estado del Proyecto

✅ **Completado:**
- Configuración base del proyecto
- Estructura hexagonal
- Modelos de dominio
- Excepciones de dominio
- Puertos (interfaces)
- DTOs y mappers
- Servicios de aplicación
- Autenticación JWT
- Controladores REST
- Swagger/OpenAPI
- Entidades JPA
- Repositorios Spring Data

⏳ **Pendiente:**
- Adaptadores de persistencia completos
- Cliente SWAPI
- Pruebas unitarias
- Pruebas de integración
- Despliegue

## 👤 Autor

[Tu Nombre]

## 📄 Licencia

MIT




