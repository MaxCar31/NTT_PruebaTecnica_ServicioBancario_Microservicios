# Prueba Técnica Backend - Arquitectura de Microservicios

Este repositorio contiene la solución a la **Prueba Técnica Backend Java v2**, enfocada en la implementación de una arquitectura de microservicios.

La solución ha sido desarrollada aplicando los principios de **Arquitectura Limpia (Hexagonal)**,
separando el dominio de la aplicación de la infraestructura y cumpliendo con los requisitos especificados para el perfil **Semi-Senior**

## 🏗️ Arquitectura General

El proyecto está compuesto por dos microservicios independientes, cada uno con su propia base de datos, siguiendo el patrón *Database-per-service*.

1.  **`customer-service` (Puerto `8080`)**
    * **Responsabilidad:** Gestionar la información de `Personas` y `Clientes`.
    * **Base de Datos:** `customer-db` (PostgreSQL)
2.  **`account-service` (Puerto `8081`)**
    * **Responsabilidad:** Gestionar `Cuentas`, `Movimientos` y generar `Reportes`.
    * **Base de Datos:** `account-db` (PostgreSQL)

### Comunicación entre Servicios

La comunicación se implementa de forma **síncrona (REST)**, tal como se solicita para el perfil Semi-Senior:

* El `account-service` consume al `customer-service` (usando `WebClient` y `Resilience4j`) para validar la existencia de un cliente antes de crear una cuenta.
* Se implementa un **Circuit Breaker** en `account-service` para manejar fallos en la comunicación con `customer-service`, aportando resiliencia al sistema.

## ✨ Funcionalidades Implementadas

Se han cubierto todas las funcionalidades requeridas en el documento:

* **[F1] CRUDs Completos:** Endpoints para la gestión de Clientes, Cuentas y Movimientos.
* **[F2] Lógica de Movimientos:** Cálculo y actualización de saldos (Débito y Crédito) .
* **[F3] Validación de Saldo:** Control de "Saldo no disponible" mediante excepciones personalizadas (`InsufficientBalanceException`)
* **[F4] Reportes:** Endpoint (`/api/v1/reports`) que genera un "Estado de Cuenta" en formato **Excel** por cliente y rango de fechas
* **[F5] Pruebas Unitarias:** Pruebas para la lógica de negocio (Casos de Uso) en `MovementUseCaseImplTest` y `AccountUseCaseImplTest`
* **[F6] Pruebas de Integración:** Prueba de integración a nivel de controlador API en `MovementControllerIntegrationTest`
* **[F7] Despliegue Docker:** La solución completa se despliega usando Docker y `docker-compose`

## 💻 Stack Tecnológico

| Categoría | Tecnología | Razón |
| :--- | :--- | :--- |
| **Lenguaje** | Java 21 | Requerido (versión 17+) |
| **Framework** | Spring Boot 3.5.7 | Requerido (última versión estable) |
| **Reactividad** | Spring WebFlux (Project Reactor) | Requerido |
| **Base de Datos** | Spring Data JPA + PostgreSQL | Requerido (JPA y BDD Relacional) |
| **Contenedores** | Docker & Docker Compose | Requerido  |
| **Documentación** | SpringDoc (Swagger) | Requerido (Contract First)  |
| **Resiliencia** | Resilience4j (Circuit Breaker) | Buena práctica (para comunicación entre servicios) |
| **Utilidades** | Lombok, MapStruct | Requerido (Lombok) y buena práctica (MapStruct) |
| **Testing** | JUnit 5, Mockito, WebTestClient | Pruebas unitarias y de integración |

## 🚀 Cómo Ejecutar la Solución

Gracias a Docker, el proyecto se puede levantar con un solo comando.

### Pre-requisitos
* Git
* Docker
* Docker Compose

### Pasos para el Despliegue

1.  **Clonar el repositorio:**
    ```bash
    git clone [URL-DE-TU-REPOSITORIO]
    cd [NOMBRE-DE-LA-CARPETA]
    ```

2.  **Construir y ejecutar con Docker Compose:**
    Este comando construirá las imágenes de ambos microservicios, creará las bases de datos y levantará toda la aplicación.

    ```bash
    docker-compose up --build
    ```

La aplicación estará lista cuando los logs muestren que ambos servicios (`customer-service` y `account-service`) se han iniciado.

---
### `docker-compose.yml`

El archivo `docker-compose.yml` está configurado para gestionar todo el entorno:

```yaml
services:
  # --- Base de Datos para Clientes ---
  customer-db:
    image: postgres:15-alpine
    container_name: customer-db
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
      - POSTGRES_DB=customer_db
    networks:
      - bank-network
    volumes:
      - customer_db_data:/var/lib/postgresql/data

  # --- Base de Datos para Cuentas ---
  account-db:
    image: postgres:15-alpine
    container_name: account-db
    ports:
      - "5433:5432"
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
      - POSTGRES_DB=account_db
    networks:
      - bank-network
    volumes:
      - account_db_data:/var/lib/postgresql/data

  # --- Microservicio de Clientes ---
  customer-service:
    container_name: customer-service
    build:
      context: ./com.bank.customer
    ports:
      - "8080:8080" # Expone en el puerto 8080
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://customer-db:5432/customer_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    networks:
      - bank-network
    depends_on:
      - customer-db

  # --- Microservicio de Cuentas ---
  account-service:
    container_name: account-service
    build:
      context: ./com.bank.account
    ports:
      - "8081:8081" # Expone en el puerto 8081
    environment:
      - SERVER_PORT=8081 
      - SPRING_DATASOURCE_URL=jdbc:postgresql://account-db:5432/account_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      # Variable para que se comunique con el otro servicio (por nombre de contenedor)
      - SERVICES_CUSTOMER_BASE_URL=http://customer-service:8080
    networks:
      - bank-network
    depends_on:
      - account-db
      - customer-service

networks:
  bank-network:
    driver: bridge

volumes:
  customer_db_data:
  account_db_data:
````

## 📖 Explorar la API (Swagger)

Una vez que los contenedores estén en ejecución, puedes acceder a la documentación de Swagger (OpenAPI) para cada servicio:

  * **Customer Service (Clientes):**
    [http://localhost:8080/swagger-ui.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui.html)

  * **Account Service (Cuentas y Movimientos):**
    [http://localhost:8081/swagger-ui.html](https://www.google.com/search?q=http://localhost:8081/swagger-ui.html)

## 🗂️ Otros Entregables

  * **Especificación OpenAPI:** Se debe compartir la especificación `openapi.yaml` (tal como se solicita en los entregables)
  * **Colección Postman:** Se debe compartir la colección de pruebas de los endpoints
  * **Script de Base de Datos:** Se debe generar un script `BaseDatos.sql`

-----

**Autor: Max Carrión**

```
