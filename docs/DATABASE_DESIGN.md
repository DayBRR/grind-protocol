# 📦 DATABASE_DESIGN.md

## 📖 Overview

Este documento define el diseño de base de datos para **Grind Protocol**, una aplicación de gamificación de tareas.

El modelo sigue estos principios:

- Separación clara entre:
    - **Seguridad / identidad**
    - **Dominio gamificado**
- Separación clara entre:
    - **Definiciones reutilizables (templates)**
    - **Instancias del usuario**
- Preparado para:
    - Escalabilidad
    - Reutilización
    - Sistema híbrido (usuario + sistema)
- Compatible con:
    - PostgreSQL
    - Arquitectura modular
    - Integración con `security-core`

> Grind Protocol reutiliza la tabla `users` ya existente del módulo de seguridad.  
> La tabla `refresh_tokens` también pertenece al módulo de seguridad y no al core domain gamificado, aunque aparece en el diagrama completo para reflejar el esquema real de la base de datos.

---

## 🧩 Diagrama ER (Mermaid)

```mermaid
erDiagram
  USERS ||--|| USER_PROFILE : has
  USERS ||--o{ REFRESH_TOKEN : owns
  USERS ||--o{ TASK : owns
  USERS ||--o{ TASK_COMPLETION : performs
  USERS ||--o{ DAILY_PROGRESS : tracks
  USERS ||--o{ REWARD : owns
  USERS ||--o{ REWARD_CLAIM : redeems
  USERS ||--o{ ALERT : receives
  USERS ||--o{ TIMELINE_EVENT : generates
  USERS ||--o{ TASK_TEMPLATE : creates
  USERS ||--o{ REWARD_TEMPLATE : creates

  TASK_TEMPLATE ||--o{ TASK : instanced_as
  TASK ||--o{ TASK_COMPLETION : completed_as

  REWARD_TEMPLATE ||--o{ REWARD : instanced_as
  REWARD ||--o{ REWARD_CLAIM : claimed_as

  USERS {
    bigint id PK
    string username
    string email
    boolean email_verified
    timestamp email_verified_at ?
    string password
    string role
    boolean enabled
    timestamp created_at
    timestamp updated_at
    timestamp last_login_at ?
    bigint created_by FK ?
    bigint updated_by FK ?
  }

  REFRESH_TOKEN {
    bigint id PK
    bigint user_id FK
    string token_hash
    timestamp created_at
    timestamp expires_at
    boolean revoked
    timestamp revoked_at ?
    string revocation_reason ?
    timestamp last_used_at ?
    string replaced_by_token_hash ?
    string ip ?
    string user_agent ?
  }

  USER_PROFILE {
    bigint id PK
    bigint user_id FK
    string display_name
    int daily_task_goal
    long total_xp
    long core_points
    int current_streak
    int best_streak
    date last_evaluated_date
    timestamp created_at
    timestamp updated_at
  }

  TASK_TEMPLATE {
    bigint id PK
    bigint creator_user_id FK ?
    string title
    string description
    enum category
    enum difficulty
    enum task_type
    int base_xp
    boolean mandatory
    boolean streak_eligible
    boolean repeatable
    int max_completions_per_day
    boolean diminishing_returns_enabled
    time due_time ?
    int weekly_closing_day ?
    boolean is_public
    boolean active
    timestamp created_at
    timestamp updated_at
  }

  TASK {
    bigint id PK
    bigint user_id FK
    bigint template_id FK ?
    string title
    string description
    enum category
    enum difficulty
    enum task_type
    int base_xp
    boolean mandatory
    boolean streak_eligible
    boolean repeatable
    int max_completions_per_day
    boolean diminishing_returns_enabled
    boolean active
    time due_time ?
    int weekly_closing_day ?
    timestamp created_at
    timestamp updated_at
  }

  TASK_COMPLETION {
    bigint id PK
    bigint task_id FK
    bigint user_id FK
    timestamp completed_at
    date completion_date
    int completion_index_for_day
    boolean counted_for_daily_goal
    boolean counted_for_streak
    int base_xp
    int awarded_xp
    int awarded_core_points
    string notes ?
    timestamp created_at
  }

  DAILY_PROGRESS {
    bigint id PK
    bigint user_id FK
    date progress_date
    int required_task_count
    int completed_valid_task_count
    int mandatory_tasks_required
    int mandatory_tasks_completed
    boolean day_qualified
    timestamp evaluated_at ?
    timestamp created_at
  }

  REWARD_TEMPLATE {
    bigint id PK
    bigint creator_user_id FK ?
    string title
    string description
    int cost_core_points
    int required_level ?
    enum availability_type
    int cooldown_days ?
    int max_claims_per_period ?
    enum period_type ?
    boolean is_public
    boolean active
    timestamp created_at
    timestamp updated_at
  }

  REWARD {
    bigint id PK
    bigint user_id FK
    bigint template_id FK ?
    string title
    string description
    int cost_core_points
    int required_level ?
    enum availability_type
    int cooldown_days ?
    int max_claims_per_period ?
    enum period_type ?
    boolean active
    timestamp created_at
    timestamp updated_at
  }

  REWARD_CLAIM {
    bigint id PK
    bigint reward_id FK
    bigint user_id FK
    timestamp claimed_at
    int cost_paid
    string notes ?
    timestamp created_at
  }

  ALERT {
    bigint id PK
    bigint user_id FK
    enum alert_type
    enum severity
    string title
    string message
    string related_entity_type ?
    bigint related_entity_id ?
    timestamp triggered_at
    timestamp read_at ?
    timestamp dismissed_at ?
    boolean active
    timestamp created_at
  }

  TIMELINE_EVENT {
    bigint id PK
    bigint user_id FK
    enum event_type
    string title
    string description
    string source_type ?
    bigint source_id ?
    json metadata_json ?
    timestamp created_at
  }
```

---

## 🧠 Conceptos clave

### Seguridad vs dominio

| Área | Propósito |
|------|-----------|
| `USERS`, `REFRESH_TOKEN` | Identidad, autenticación, sesiones, seguridad |
| `USER_PROFILE` y resto del dominio | Progreso gamificado, tareas, recompensas, historial |

### Templates vs instancias

| Tipo | Propósito |
|------|-----------|
| `*_TEMPLATE` | Definiciones reutilizables del sistema o de usuarios |
| `TASK`, `REWARD` | Instancias reales del usuario |

---

## 🧩 Entidades

### 👤 USERS

Entidad base de autenticación e identidad del sistema.

Sirve para:
- representar al usuario a nivel global
- actuar como propietario de las entidades del dominio
- enlazar Grind Protocol con `security-core`
- soportar autenticación, autorización y auditoría básica

Campos relevantes:
- `username`, `email`, `password`, `role`, `enabled`
- `email_verified`, `email_verified_at`
- `created_at`, `updated_at`, `last_login_at`
- `created_by`, `updated_by`

Notas:
- `created_by` y `updated_by` son autoreferencias a `users.id`
- pueden ser `NULL` en seeds, primer usuario o procesos automáticos
- `email` pertenece a identidad/auth, no al perfil gamificado
- `email_verified` indica si el correo fue validado
- `email_verified_at` guarda la fecha de verificación cuando exista

---

### 🔐 REFRESH_TOKEN

Tabla del módulo de seguridad para gestión de sesiones y refresh tokens.

Sirve para:
- almacenar refresh tokens de forma segura
- soportar expiración, rotación y revocación
- registrar información útil de seguridad como IP y user-agent

Notas:
- pertenece al módulo de seguridad
- no forma parte del core gamificado
- ya incorpora un nivel importante de auditoría operativa

---

### 🧬 USER_PROFILE

Estado gamificado agregado del usuario.

Sirve para almacenar:
- XP total
- Core Points
- racha actual
- mejor racha
- mínimo diario configurado
- última fecha evaluada

No es la fuente de verdad histórica, sino un agregado de estado.

Objetivo:
- separar identidad/autenticación de progreso de producto
- permitir evolucionar Grind Protocol sin tocar el módulo de seguridad

---

## 🧱 TASK DOMAIN

### 📋 TASK_TEMPLATE

Definición reutilizable de tareas.

Sirve para:
- crear tareas del sistema
- permitir que usuarios creen plantillas
- compartir tareas con otros usuarios
- preparar un catálogo o marketplace futuro

Reglas importantes:
- `creator_user_id = NULL` → plantilla del sistema
- `is_public = true` → plantilla visible para otros usuarios

---

### ✅ TASK

Instancia real de tarea de un usuario.

Sirve para:
- representar la tarea que el usuario ejecuta de verdad
- guardar configuración propia e independiente
- soportar tareas manuales o creadas desde plantilla

Reglas importantes:
- `template_id = NULL` → tarea creada manualmente
- `template_id != NULL` → tarea creada desde plantilla
- al crearla desde plantilla, los datos se copian y luego queda desacoplada

---

### ✔ TASK_COMPLETION

Evento de ejecución de una tarea.

Sirve para registrar:
- cuándo se completó
- si fue repetición
- cuánta XP se otorgó realmente
- cuántos Core Points generó
- si contó para el mínimo diario
- si contó para racha

Es la fuente de verdad del sistema para progreso e histórico.

---

### 📅 DAILY_PROGRESS

Resumen y evaluación del día de un usuario.

Sirve para:
- saber si el día fue válido
- comprobar mínimo diario y tareas obligatorias
- preparar lógica de racha y validación final del día

---

## 🎁 REWARD DOMAIN

### 🎯 REWARD_TEMPLATE

Definición reutilizable de recompensas.

Sirve para:
- recompensas del sistema
- recompensas creadas por usuarios
- recompensas compartibles
- futuro catálogo de recompensas sugeridas

Reglas importantes:
- `creator_user_id = NULL` → plantilla del sistema
- `is_public = true` → plantilla visible para otros usuarios

---

### 🏆 REWARD

Instancia real de recompensa disponible para un usuario.

Sirve para:
- representar lo que el usuario puede canjear
- mantener independencia frente a la plantilla original
- soportar recompensas manuales o creadas desde plantilla

Reglas importantes:
- `template_id = NULL` → recompensa creada manualmente
- `template_id != NULL` → recompensa creada desde plantilla
- al crearla desde plantilla, los datos se copian y luego queda desacoplada

---

### 💰 REWARD_CLAIM

Evento de canje de recompensa.

Sirve para registrar:
- cuándo se canjeó
- cuánto costó
- observaciones opcionales

Es la base para límites por período, cooldowns y analítica.

---

## 🔔 ALERT SYSTEM

### ⚠ ALERT

Alerta generada por el sistema.

Sirve para:
- avisar de riesgo de perder racha
- alertar sobre tareas pendientes o a punto de caducar
- informar de progreso cercano a objetivos
- mostrar oportunidades de acción

---

## 📜 TIMELINE

### 🧾 TIMELINE_EVENT

Evento histórico del usuario.

Sirve para construir:
- historial de actividad
- narrativa visual del progreso
- trazabilidad ligera de acciones relevantes

---

## 🔗 Relaciones clave

- `USERS` → `USER_PROFILE` (1:1)
- `USERS` → `REFRESH_TOKEN` (1:N)
- `USERS` → `TASK_TEMPLATE` (1:N)
- `USERS` → `TASK` (1:N)
- `TASK_TEMPLATE` → `TASK` (1:N)
- `TASK` → `TASK_COMPLETION` (1:N)
- `USERS` → `DAILY_PROGRESS` (1:N)
- `USERS` → `REWARD_TEMPLATE` (1:N)
- `USERS` → `REWARD` (1:N)
- `REWARD_TEMPLATE` → `REWARD` (1:N)
- `REWARD` → `REWARD_CLAIM` (1:N)
- `USERS` → `ALERT` (1:N)
- `USERS` → `TIMELINE_EVENT` (1:N)

---

## ⚙️ Decisiones importantes

### 1. Reutilización de `users`
Grind Protocol no crea una nueva tabla de identidad.  
Reutiliza la tabla `users` ya existente del módulo de seguridad.

Esto permite:
- evitar duplicidades
- mantener un único origen de verdad para identidad
- integrar el dominio gamificado con la autenticación existente

### 2. Separación `USERS` vs `USER_PROFILE`
Permite:
- desacoplar seguridad y dominio gamificado
- mantener identidad y progreso como conceptos distintos
- escalar mejor el diseño futuro sin tocar auth

### 3. Uso de templates
Permite:
- tareas y recompensas del sistema
- contenido reutilizable creado por usuarios
- escalado futuro hacia compartición o marketplace

### 4. Copia de datos desde template
Cuando un usuario crea una `TASK` o `REWARD` desde un template:
- se copian los campos
- la instancia resultante queda independiente

Esto evita dependencia runtime de la plantilla original.

### 5. Nullable
Los campos marcados con `?` en el diagrama son `nullable`.

Especialmente importantes:
- `email_verified_at`
- `last_login_at`
- `created_by`
- `updated_by`
- `creator_user_id`
- `template_id`
- `due_time`
- `weekly_closing_day`
- `required_level`
- `cooldown_days`
- `max_claims_per_period`
- `period_type`
- `notes`
- `related_entity_type`
- `related_entity_id`
- `read_at`
- `dismissed_at`
- `source_type`
- `source_id`
- `metadata_json`

### 6. Auditoría en `users`
La tabla `users` incorpora:
- `created_at`
- `updated_at`
- `last_login_at`
- `created_by`
- `updated_by`

Esto la deja preparada para:
- trazabilidad administrativa
- paneles internos futuros
- seguimiento básico de cambios sobre identidad

### 7. Verificación de email
La tabla `users` incorpora:
- `email_verified`
- `email_verified_at`

Esto la deja preparada para:
- validación de cuenta
- recuperación segura
- flujos futuros de onboarding y seguridad

---

## 🚀 Futuras mejoras

- `LEVEL_SYSTEM`
- `ACHIEVEMENTS`
- `STREAK_SAVERS`
- `SOCIAL / RANKING`
- `ANTI-CHEAT RULES`
- posible `USER_STATS`
- posible `DEBUFFS`

---

## 🧭 Estado actual

✔ Diseño estable para MVP  
✔ Compatible con arquitectura modular  
✔ Integrado con el módulo de seguridad real  
✔ Preparado para escalar  
✔ Listo para implementación del `task-domain`
