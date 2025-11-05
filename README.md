# 📝 README.md Completo

```markdown
# 🏦 Banking System - Microservices Architecture

Sistema bancario basado en microservicios para la gestión de clientes, cuentas y movimientos financieros. Implementado con Spring Boot, arquitectura hexagonal y programación reactiva.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Uso](#-uso)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Patrones de Diseño](#-patrones-de-diseño)
- [Monitoreo y Observabilidad](#-monitoreo-y-observabilidad)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Roadmap](#-roadmap)
- [Contribución](#-contribución)
- [Licencia](#-licencia)
- [Contacto](#-contacto)

---

## ✨ Características

### Microservicio de Clientes (Customer Service)
- ✅ Gestión completa de clientes (CRUD)
- ✅ Validación de datos con Bean Validation
- ✅ Encriptación de contraseñas con BCrypt
- ✅ Prevención de registros duplicados
- ✅ Eliminación lógica (soft delete)

### Microservicio de Cuentas (Account Service)
- ✅ Gestión de cuentas bancarias (CRUD)
- ✅ Registro de movimientos (débitos/créditos)
- ✅ Validación de saldo disponible
- ✅ Libro contable (Ledger) para auditoría
- ✅ Generación de reportes en Excel
- ✅ Comunicación con Customer Service
- ✅ Patrones de resiliencia (Circuit Breaker, Retry, Timeout)

### Características Técnicas
- 🔄 **Programación Reactiva** con Spring WebFlux (Mono/Flux)
- 🏗️ **Arquitectura Hexagonal** (Ports & Adapters)
- 🐳 **Containerización** con Docker
- 📝 **Documentación** automática con OpenAPI/Swagger
- ✅ **Testing** completo (Unitario + Integración)

---

## 🏗️ Arquitectura

### Vista General del Sistema

```
┌─────────────────┐         ┌─────────────────┐
│                 │         │                 │
│  Customer       │◄────────┤  Account        │
│  Service        │  REST   │  Service        │
│  (Port 8081)    │         │  (Port 8080)    │
│                 │         │                 │
└────────┬────────┘         └────────┬────────┘
         │                           │
         │                           │
    ┌────▼────┐                 ┌────▼────┐
    │Customer │                 │Account  │
    │   DB    │                 │   DB    │
    │(Pg:5432)│                 │(Pg:5433)│
    └─────────┘                 └─────────┘
                    │
                    │
              ┌─────▼─────┐
              │  Zipkin   │
              │ (Port     │
              │  9411)    │
              └───────────┘
```

### Arquitectura Hexagonal

```
┌──────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                   │
│  ┌────────────────────────────────────────────────┐  │
│  │              Use Cases (Business Logic)        │  │
│  └────────────────────────────────────────────────┘  │
│                ▲                        │             │
│                │                        │             │
│        ┌───────┴────────┐      ┌───────▼────────┐   │
│        │  Input Ports   │      │  Output Ports  │   │
│        │  (Interfaces)  │      │  (Interfaces)  │   │
│        └────────────────┘      └────────────────┘   │
└──────────────────────────────────────────────────────┘
         ▲                                │
         │                                │
┌────────┴────────┐              ┌───────▼───────┐
│ INFRASTRUCTURE  │              │INFRASTRUCTURE │
│                 │              │               │
│  REST API       │              │  JPA          │
│  Controllers    │              │  Repositories │
│  (Adapters)     │              │  (Adapters)   │
└─────────────────┘              └───────────────┘
```

---

## 🛠️ Tecnologías

### Backend
- **Java 17** - Lenguaje de programación
- **Spring Boot 3.5.7** - Framework principal
- **Spring WebFlux** - Programación reactiva
- **Spring Data JPA** - Persistencia de datos
- **PostgreSQL 15** - Base de datos relacional

### Comunicación & Resiliencia
- **WebClient** - Cliente HTTP reactivo
- **OpenAPI/Swagger** - Documentación de API


### Utilities
- **Lombok** - Reducción de boilerplate
- **MapStruct** - Mapeo de objetos
- **Apache POI** - Generación de archivos Excel

### Testing
- **JUnit 5** - Framework de pruebas
- **Mockito** - Mocking


### DevOps
- **Docker & Docker Compose** - Containerización
- **Gradle** - Gestión de dependencias

---

## 📦 Requisitos Previos

Asegúrate de tener instalado:

- ☕ **Java 17** o superior
  ```bash
  java -version
  ```

- 🐳 **Docker & Docker Compose**
  ```bash
  docker --version
  docker-compose --version
  ```

- 🔧 **Gradle 8.x** (opcional, el proyecto incluye Gradle Wrapper)
  ```bash
  ./gradlew --version
  ```

---

## 🚀 Instalación

### 1. Clonar el Repositorio

```bash
git clone https://github.com/MaxCar31/NTT_PruebaTecnica_ServicioBancario_Microservicios.git
cd banking-system
```

### 2. Levantar la Infraestructura

```bash
# Iniciar bases de datos y Zipkin
docker-compose up -d

# Verificar que los contenedores estén corriendo
docker-compose ps
```

### 3. Compilar los Microservicios

#### Customer Service
```bash
cd customer-service
./gradlew clean build
```

#### Account Service
```bash
cd account-service
./gradlew clean build
```

### 4. Ejecutar los Microservicios

#### Opción A: Usando Gradle

**Terminal 1 - Customer Service:**
```bash
cd customer-service
./gradlew bootRun
```

**Terminal 2 - Account Service:**
```bash
cd account-service
./gradlew bootRun
```

#### Opción B: Usando JAR compilado

```bash
# Customer Service
java -jar customer-service/build/libs/customer-service-0.0.1-SNAPSHOT.jar

# Account Service
java -jar account-service/build/libs/account-service-0.0.1-SNAPSHOT.jar
```

### 5. Verificar la Instalación

Espera unos segundos y verifica que los servicios estén corriendo:

```bash
# Customer Service Health
curl http://localhost:8081/actuator/health

# Account Service Health
curl http://localhost:8080/actuator/health
```

---

## ⚙️ Configuración

### Variables de Entorno

Puedes sobrescribir la configuración usando variables de entorno:

#### Customer Service
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/customer_db
export SPRING_DATASOURCE_USERNAME=customer_user
export SPRING_DATASOURCE_PASSWORD=customer_pass
export SERVER_PORT=8081
```

#### Account Service
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/account_db
export SPRING_DATASOURCE_USERNAME=account_user
export SPRING_DATASOURCE_PASSWORD=account_pass
export SERVER_PORT=8080
export CUSTOMER_SERVICE_URL=http://localhost:8081
```

### Profiles de Spring

El proyecto soporta diferentes perfiles:

- `default` - Desarrollo local
- `test` - Pruebas automatizadas
- `prod` - Producción (requiere configuración adicional)

```bash
# Ejecutar con perfil específico
./gradlew bootRun --args='--spring.profiles.active=prod'
```

---

## 💻 Uso

### Acceso a las Interfaces

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Customer Service - Swagger | http://localhost:8081/swagger-ui.html | Documentación interactiva |
| Customer Service - API Docs | http://localhost:8081/api-docs | Especificación OpenAPI |
| Account Service - Swagger | http://localhost:8080/swagger-ui.html | Documentación interactiva |
| Account Service - API Docs | http://localhost:8080/api-docs | Especificación OpenAPI |
| Zipkin UI | http://localhost:9411 | Trazabilidad distribuida |

### Ejemplos de Uso con cURL

#### 1. Crear un Cliente

```bash
curl -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jose Lema",
    "gender": "M",
    "identification": "1234567890",
    "address": "Otavalo sn y principal",
    "phone": "098254785",
    "password": "Passw0rd!"
  }'
```

#### 2. Obtener Cliente por ID

```bash
curl -X GET http://localhost:8081/api/v1/customers/1
```

#### 3. Crear una Cuenta

```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "1234567890",
    "accountType": "SAVINGS",
    "initialBalance": 2000,
    "status": true,
    "customerId": 1
  }'
```

#### 4. Registrar un Movimiento (Débito)

```bash
curl -X POST http://localhost:8080/api/v1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "amount": -575
  }'
```

#### 5. Registrar un Movimiento (Crédito)

```bash
curl -X POST http://localhost:8080/api/v1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "amount": 600
  }'
```

#### 6. Generar Reporte de Estado de Cuenta

```bash
curl -X GET "http://localhost:8080/api/v1/reports?clientId=1&startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59" \
  -o reporte.xlsx
```

---

## 📚 API Documentation

La documentación completa de las APIs está disponible a través de Swagger UI:

### Customer Service API

**Base URL:** `http://localhost:8081/api/v1`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/customers` | Crear nuevo cliente |
| GET | `/customers` | Listar todos los clientes |
| GET | `/customers/{id}` | Obtener cliente por ID |
| PUT | `/customers/{id}` | Actualizar cliente |
| DELETE | `/customers/{id}` | Eliminar cliente (lógico) |

### Account Service API

**Base URL:** `http://localhost:8080/api/v1`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/accounts` | Crear nueva cuenta |
| GET | `/accounts` | Listar todas las cuentas |
| GET | `/accounts/{id}` | Obtener cuenta por ID |
| GET | `/accounts/search?accountNumber={number}` | Buscar cuenta por número |
| PUT | `/accounts/{id}` | Actualizar cuenta |
| DELETE | `/accounts/{id}` | Eliminar cuenta (lógico) |
| POST | `/movements` | Registrar movimiento |
| GET | `/movements/all` | Listar todos los movimientos |
| GET | `/movements/by-account?accountId={id}` | Movimientos por cuenta |
| GET | `/movements/{id}` | Obtener movimiento por ID |
| DELETE | `/movements/{id}` | Eliminar movimiento |
| GET | `/reports` | Generar reporte de cuenta |

Para más detalles, consulta la [Especificación OpenAPI](openapi.yaml).

---

## 🧪 Testing

### Ejecutar Todas las Pruebas

```bash
# Customer Service
cd customer-service
./gradlew test

# Account Service
cd account-service
./gradlew test
```

### Ejecutar Pruebas con Reporte de Cobertura

```bash
./gradlew test jacocoTestReport

# Ver reporte en:
# build/reports/jacoco/test/html/index.html
```

### Pruebas de Integración

Las pruebas de integración usan **Testcontainers** para levantar una instancia real de PostgreSQL:

```bash
./gradlew integrationTest
```

### Estructura de Pruebas

```
src/test/java/
├── unit/
│   ├── AccountUseCaseImplTest.java
│   ├── CustomerUseCaseImplTest.java
│   ├── MovementUseCaseImplTest.java
│   └── ReportUseCaseImplTest.java
└── integration/
    ├── AccountIntegrationTest.java
    ├── CustomerIntegrationTest.java
    └── MovementIntegrationTest.java
```

---

## 🎨 Patrones de Diseño

### Arquitecturales
- **Hexagonal Architecture (Ports & Adapters)** - Separación de capas
- **Microservices** - Servicios independientes y escalables
- **CQRS** (Preparado) - Separación de comandos y consultas

### Resiliencia
- **Circuit Breaker** - Prevención de fallos en cascada
- **Retry** - Reintentos automáticos
- **Timeout** - Límites de tiempo de espera
- **Rate Limiter** - Control de tasa de peticiones

### Código
- **Repository Pattern** - Abstracción de persistencia
- **Dependency Injection** - Inversión de dependencias
- **Factory Pattern** - Creación de objetos
- **Strategy Pattern** - Algoritmos intercambiables
- **DTO Pattern** - Objetos de transferencia

---

## 📊 Monitoreo y Observabilidad

### Actuator Endpoints

Ambos servicios exponen endpoints de monitoreo:

```bash
# Health Check
curl http://localhost:8080/actuator/health

# Métricas
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

### Trazabilidad Distribuida

Accede a Zipkin para ver las trazas distribuidas:

**URL:** http://localhost:9411

Ejemplo de flujo trazado:
```
Customer Request → Account Service → Customer Service
```

### Métricas Disponibles

- `http.server.requests` - Peticiones HTTP
- `jvm.memory.used` - Uso de memoria
- `jvm.threads.live` - Threads activos
- `resilience4j.circuitbreaker.calls` - Llamadas al Circuit Breaker

---

## 📁 Estructura del Proyecto

```
banking-system/
├── customer-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bank/customer/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   └── exception/
│   │   │   │   ├── application/
│   │   │   │   │   ├── input/port/
│   │   │   │   │   ├── output/port/
│   │   │   │   │   └── service/
│   │   │   │   └── infrastructure/
│   │   │   │       ├── config/
│   │   │   │       ├── input/adapter/rest/
│   │   │   │       ├── output/adapter/jpa/
│   │   │   │       └── exception/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── build.gradle
│   └── Dockerfile
├── account-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bank/account/
│   │   │   │   ├── domain/
│   │   │   │   ├── application/
│   │   │   │   └── infrastructure/
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   ├── build.gradle
│   └── Dockerfile
├── docker-compose.yml
├── .gitignore
├── README.md
└── openapi.yaml
```

## 🤝 Contribución

¡Las contribuciones son bienvenidas! Por favor, sigue estos pasos:

1. **Fork** el proyecto
2. Crea una **rama** para tu feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. **Push** a la rama (`git push origin feature/AmazingFeature`)
5. Abre un **Pull Request**

### Guía de Estilo

- Sigue los principios SOLID
- Escribe pruebas para nuevo código
- Documenta las APIs con OpenAPI
- Usa Conventional Commits
- Mantén el código limpio y legible

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.


---

## 🙏 Agradecimientos

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Project Reactor](https://projectreactor.io/)
- [Resilience4j](https://resilience4j.readme.io/)
- [Testcontainers](https://www.testcontainers.org/)
- [OpenAPI](https://www.openapis.org/)

---

