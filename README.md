# ⚙️ grind-protocol

**grind-protocol** es una aplicación Spring Boot que consume la librería
`security-core` para validar su reutilización en un entorno real.

Este proyecto actúa como una **app consumidora independiente**,
demostrando que el módulo de seguridad puede integrarse de forma limpia
y desacoplada.

------------------------------------------------------------------------

## 🎯 Objetivo

Validar el uso de `security-core` fuera de su contexto original,
aportando infraestructura propia:

-   🧩 Entidades JPA\
-   🗃️ Repositorios\
-   🔌 Adapters\
-   ⚙️ Configuración Spring\
-   🧪 Flyway (migraciones)\
-   🐘 Base de datos PostgreSQL

------------------------------------------------------------------------

## 🚀 Tech Stack

-   ☕ **Java 17**\
-   🌱 **Spring Boot 3.5.x**\
-   🔐 **Spring Security**\
-   🗄️ **Spring Data JPA**\
-   🐘 **PostgreSQL**\
-   🧪 **Flyway**\
-   📄 **OpenAPI / Swagger**\
-   🐳 **Docker / Docker Compose**

------------------------------------------------------------------------

## 🔗 Security Module

This project uses:

👉 security-core v0.1.0  
https://github.com/DayBRR/security-core

``` xml
<dependency>
  <groupId>com.davidrr</groupId>
  <artifactId>security-core</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

------------------------------------------------------------------------

## 🧱 Qué aporta este proyecto

Implementa la capa específica del dominio consumidor:

-   👤 `User`\
-   🔄 `RefreshToken`\
-   📦 `UserRepository`\
-   📦 `RefreshTokenRepository`\
-   🔐 `UserDetailsServiceImpl`\
-   🧠 `JpaRefreshSessionStore`\
-   🔌 `SpringSecurityUserProvider`\
-   ⚙️ Configuración YAML\
-   🧪 Migraciones Flyway

------------------------------------------------------------------------

## 🏗️ Estructura del proyecto

    src/main/java/com/davidrr/grindprotocol/
    ├── GrindProtocolApplication.java
    ├── security/
    │   └── auth/
    │       └── controller/
    ├── user/
    │   ├── adapter/
    │   ├── model/
    │   ├── repository/
    │   └── service/

------------------------------------------------------------------------

## 🧩 Git Workflow --- Commit & Branch Conventions

Este proyecto usa **Conventional Commits**.

### 📌 Tipos de commit

| Type       | Meaning                                            |
|------------|----------------------------------------------------|
| **feat**   | Nueva funcionalidad                                |
| **fix**    | Corrección de bug                                  |
| **refactor** | Mejora interna                                     |
| **chore**  | Mantenimiento                                      |
| **docs**   | Documentación                                      |
| **style**  | Estilo                                             |
| **test**   | Incluir o modificar tests                          |
| **build**  | Construcción cambios del sistema (Maven, plugins…) |


------------------------------------------------------------------------

### 📌 Formato de commit

    <type>: <short description>

------------------------------------------------------------------------

## ▶️ Ejecución

``` bash
mvn clean package
mvn spring-boot:run
```

------------------------------------------------------------------------

## 🐳 Docker

``` bash
docker build -t grind-protocol:latest .
docker compose up --build
```

------------------------------------------------------------------------

## 👨‍💻 Author

David Ruiz https://www.davidrr.com
