# Grind Protocol — Guía de perfiles Docker y ejecución local

## Nombre recomendado para la nueva rama

Te sugiero una de estas dos opciones:

- `chore/docker-profiles-setup`
- `chore/local-docker-config`

Mi recomendación principal es:

```bash
git checkout -b chore/docker-profiles-setup
```

La usaría porque estos cambios son de configuración e infraestructura de desarrollo, no de lógica funcional de negocio.

---

## Qué se ha reorganizado

Ahora la idea es separar claramente los contextos de ejecución:

- `application.yaml` → configuración común.
- `application-local.yaml` → para arrancar el backend desde IntelliJ o por terminal en tu máquina.
- `application-docker.yaml` → para arrancar el backend dentro de Docker.
- `application-test.yaml` → para tests unitarios / rápidos con H2.
- `application-integration-test.yaml` → para tests de integración.

Esto evita que un mismo perfil signifique cosas distintas según el entorno.

---

## Cómo usar cada perfil

### 1. Perfil `local`

Usa este perfil cuando:

- levantas PostgreSQL con Docker
- ejecutas la aplicación desde IntelliJ en modo Run o Debug

Este perfil normalmente apunta a:

```text
jdbc:postgresql://localhost:5432/grind_protocol
```

### Cómo lanzarlo en IntelliJ

En la Run Configuration del proyecto:

- **Active profiles**: `local`

O como variable de entorno:

```text
SPRING_PROFILES_ACTIVE=local
```

---

### 2. Perfil `docker`

Usa este perfil cuando:

- levantas toda la aplicación con `docker-compose`
- el backend corre dentro del contenedor Docker

Este perfil normalmente apunta a:

```text
jdbc:postgresql://postgres:5432/grind_protocol
```

Aquí `postgres` es el nombre del servicio Docker.

---

### 3. Perfil `test`

Usa este perfil para tests rápidos con H2 en memoria.

Normalmente:

- `ddl-auto: create-drop`
- Flyway desactivado
- rate limit desactivado

Se usa en tests unitarios o de capa web donde no quieres depender de PostgreSQL real.

---

### 4. Perfil `integration-test`

Usa este perfil para tests de integración más realistas.

Normalmente:

- Flyway activado
- configuración más cercana al entorno real
- rate limit desactivado para no interferir con las pruebas

---

## Cómo trabajar en el día a día

### Opción recomendada

La forma más cómoda de trabajar es esta:

1. Levantar solo PostgreSQL con Docker.
2. Ejecutar la app desde IntelliJ con perfil `local`.
3. Usar Debug en IntelliJ para poner breakpoints.

### Comando para levantar solo PostgreSQL

Desde la raíz del proyecto:

```bash
docker-compose up -d postgres
```

### Luego arrancas la app desde IntelliJ

Con perfil:

```text
local
```

Esto te da:

- base de datos real
- desarrollo rápido
- debug cómodo
- menos fricción que debug remoto en Docker

---

## Cómo levantar todo con Docker

Si quieres probar el entorno completo containerizado:

### 1. Compilar el proyecto

```bash
mvn clean package
```

### 2. Levantar los contenedores

Desde la raíz del proyecto:

```bash
docker-compose up --build
```

Esto debe ejecutarse en la carpeta donde están:

- `docker-compose.yml`
- `Dockerfile`
- `target/` con el `.jar` generado

---

## Cómo probar que los cambios funcionan

## Prueba 1 — PostgreSQL levanta correctamente

Ejecuta:

```bash
docker-compose up -d postgres
```

Luego comprueba:

```bash
docker ps
```

Deberías ver el contenedor de PostgreSQL en ejecución.

Si quieres ver logs:

```bash
docker-compose logs -f postgres
```

---

## Prueba 2 — La app arranca en IntelliJ con perfil `local`

Con PostgreSQL ya levantado:

1. Abre IntelliJ.
2. Selecciona el perfil `local`.
3. Arranca en Debug o Run.

Debes comprobar que:

- Spring Boot arranca sin errores
- conecta a PostgreSQL
- Flyway ejecuta o valida correctamente
- no falla el datasource

### Qué deberías revisar en logs

- conexión correcta a PostgreSQL
- inicialización del contexto Spring
- carga de seguridad
- arranque en el puerto `8080`

---

## Prueba 3 — Probar un endpoint de la API

Como tienes:

```text
server.servlet.context-path=/api
```

tu base path será algo como:

```text
http://localhost:8080/api
```

Prueba desde navegador, Postman o Swagger el endpoint que uses normalmente, por ejemplo:

```text
http://localhost:8080/api/swagger-ui/index.html
```

O algún endpoint de health, auth o prueba que ya tengas implementado.

---

## Prueba 4 — Arranque completo en Docker

Compila:

```bash
mvn clean package
```

Luego:

```bash
docker-compose up --build
```

Debes comprobar que:

- PostgreSQL arranca correctamente
- la app espera a que PostgreSQL esté healthy
- la app arranca sin errores
- puedes acceder a la API en:

```text
http://localhost:8080/api
```

---

## Prueba 5 — Validar que cada perfil usa su host correcto

### En `local`

La app debe usar:

```text
localhost:5432
```

### En `docker`

La app debe usar:

```text
postgres:5432
```

Si esto está bien separado, ya has dejado solucionado el problema principal de configuración.

---

## Comandos útiles

### Levantar solo PostgreSQL

```bash
docker-compose up -d postgres
```

### Levantar todo

```bash
docker-compose up --build
```

### Parar contenedores

```bash
docker-compose down
```

### Parar y borrar también volúmenes

```bash
docker-compose down -v
```

> Ojo: esto borra los datos persistidos de PostgreSQL.

### Ver logs

```bash
docker-compose logs -f
```

### Ver solo logs del backend

```bash
docker-compose logs -f app
```

### Ver solo logs de PostgreSQL

```bash
docker-compose logs -f postgres
```

---

## Flujo recomendado de trabajo

### Desarrollo normal

```bash
docker-compose up -d postgres
```

Después:

- arrancar backend desde IntelliJ
- perfil `local`
- usar Debug de IntelliJ

### Prueba de entorno completo

```bash
mvn clean package
docker-compose up --build
```

---

## Siguiente paso recomendable

Cuando confirmes que todo funciona, te recomiendo hacer:

```bash
git status
git add .
git commit -m "chore: separate local and docker runtime profiles"
```

Y luego subir la rama:

```bash
git push -u origin chore/docker-profiles-setup
```

