# BUG-PROG-001: cualificación provisional y cierre diario

## Decisión funcional

Se aplica el modelo híbrido de ROADMAP, secciones 7, 8, 15 y 20:

- `dayQualified` de hoy indica cumplimiento provisional: mínimo de tres primeras
  completaciones válidas de tareas distintas y todas las obligatorias activas completas.
- Una nueva tarea activa obligatoria cuenta **desde su creación, ese mismo día**,
  incluso si ya se había alcanzado 3/3. Se aplica tanto a creación manual como desde
  plantilla. La creación de una plantilla por sí sola no crea obligaciones.
- Una nueva obligación puede cambiar `dayQualified` a false; los completados válidos
  siguen siendo 3 y el mínimo sigue siendo 3. Otra opcional no sustituye la obligación.
- `currentStreak`, `bestStreak` y `lastEvaluatedDate` reflejan exclusivamente días
  cerrados. Hoy nunca concede un incremento definitivo ni rompe definitivamente la racha.
- Un día termina al comienzo del siguiente día local: se incluye todo el intervalo
  de las 23:59, no se cierra al empezar ese minuto. El cierre definitivo consume la
  última instantánea persistida, sin volver a consultar las definiciones de las tareas.

Las tareas activas obligatorias siguen participando como en el código anterior,
sin filtrar por `taskType`, `dueTime`, `weeklyClosingDay` o `streakEligible`.
No se introduce un nuevo motor de recurrencia. El mínimo fijo de tres también se
conserva; la personalización mediante `dailyTaskGoal` requiere un cambio separado.

## Arquitectura y persistencia

`TaskServiceImpl` y `TaskCompletionServiceImpl` cierran días vencidos bajo el bloqueo
pesimista de `UserProfile` antes de leer/escribir tareas o completados. Después
recalculan la instantánea de hoy en la misma transacción. Las recompensas de XP/CP
siguen siendo inmediatas mediante `ProgressionServiceImpl` y no dependen de cualificar.

`DailyProgressServiceImpl` solo recalcula la fecha abierta. Para una fecha pasada
devuelve la instantánea guardada, sin modificar contadores ni `evaluatedAt`; si no
existe, devuelve el error de recurso no encontrado. Rechaza fechas futuras.
El GET de hoy también recalcula y recupera cierres pendientes, por lo que puede
persistir progreso y actualizar la proyección de racha. No altera completados ni XP.

`StreakServiceImpl` consume las instantáneas pendientes en orden ascendente. Una
fecha cualificada consecutiva incrementa la racha; una no cualificada la pone a
cero y conserva el récord. Un salto sin actividad rompe la continuidad. Si no
existe instantánea para la última fecha a cerrar, se guarda una no cualificada:
no hubo suficientes completados. Sus contadores de obligaciones se inicializan a
cero, sin inventar un conjunto histórico que no se capturó. Los huecos intermedios
no se materializan fila a fila; sí rompen la continuidad de la racha.

`lastEvaluatedDate` es un límite monotónico: ni reintentos ni peticiones de fechas
anteriores vuelven a aplicar días. Snapshot y perfil se guardan en una transacción.
Se evalúan los logros después de actualizar la racha; no hace falta otra completación
para reconocer un logro basado en racha.

No se necesita migración ni dependencia nueva. Se reutilizan los contadores diarios,
la unicidad usuario/fecha y la fecha del perfil. La distinción provisional/histórica
se deriva de la fecha de negocio; no se añade un estado al DTO.

### Concurrencia

Orden: **UserProfile -> progreso/completados -> instancias de logros/quests**.
El bloqueo dura hasta commit/rollback y serializa creación, validación del índice
de completado, recompensas y cierre. Los claims existentes conservan perfil antes
de instancia; los claims de quests fijan su periodo después de adquirir el perfil.
Varios nodos pueden intentar cerrar el mismo día: PostgreSQL y el límite monotónico
evitan incrementos duplicados. No se cambia `security-core`.

Un único instante de `DayCalendar` se fija por transacción después del bloqueo para
estas operaciones. Una transacción que cruza medianoche pertenece al día en el que
obtuvo el bloqueo; un cierre esperará su commit y consumirá su resultado. Una
transacción que esperaba el bloqueo muestrea el reloj después de obtenerlo.

## Zona, periodos y ejecución del cierre

Todavía no existe una zona por usuario. `GRIND_DAY_ZONE`, por ejemplo
`Europe/Madrid`, fija una zona IANA común. Sin configuración se conserva la zona de
la JVM anterior. Debe fijarse igual en todos los nodos antes del despliegue y no
cambiarse durante la operación: un cambio de zona sobre datos existentes requiere
un plan específico de transición.

El calendario se comparte para fecha de completado, progreso, enfoque por categorías,
periodos de quests y fecha de referencia predeterminada del resumen semanal. Las
semanas siguen empezando en lunes. Los cierres avanzan por `LocalDate`, no por bloques
de 24 horas; los días de cambio horario pueden durar 23 o 25 horas.
Los timestamps de auditoría y otros módulos conservan sus mecanismos previos; no se
reinterpretan timestamps históricos al configurar la zona.

`DayClosingJob` recorre usuarios en páginas de 200, con una transacción por usuario,
cada 60 segundos. Cierra hasta ayer, recupera días tras una parada y continúa si falla
un usuario; registra el fallo y reintenta en la próxima pasada. El periodo es un
fixed delay entre ejecuciones: no garantiza cierre exactamente a medianoche ni una
latencia máxima si la pasada es lenta. Hasta que se ejecute, las consultas directas
de perfil/resumen pueden seguir mostrando la última consolidación. Consultar progreso
de hoy, crear o completar tareas recupera el cierre de ese usuario de forma síncrona.

`GRIND_DAY_CLOSING_ENABLED=false` desactiva únicamente el proceso periódico. Los
tests lo desactivan e invocan el cierre explícitamente contra PostgreSQL/Testcontainers.

## Compatibilidad de API

Las rutas y las formas JSON se conservan. No hay cambios en frontend.

- `GET /api/me/daily-progress/today`: cualificación provisional y contadores actuales.
- `POST /api/me/progression/finalize-day`: sin `date`, cierra pendientes hasta ayer.
- Con una fecha pasada, cierra pendientes hasta esa fecha; reintentos y fechas ya
  cubiertas devuelven 204 sin duplicar efectos. Un día sin actividad puede cerrarse.
- Una fecha de hoy o futura devuelve 400, código `DAY_NOT_CLOSED`.
- Rachas del perfil y resumen excluyen el día abierto de hoy.

Este cambio semántico es necesario para impedir la consolidación anticipada.
Los consumidores Angular revisados leen los mismos campos y no llaman a
`finalize-day`. El texto actual del Dashboard sigue diciendo “Day qualified”:
representa cumplimiento provisional de hoy, aunque no incluya esa aclaración visual.

## Transición desde el modelo anterior

No ejecutar versiones antigua y nueva simultáneamente: la antigua aún puede
consolidar hoy. Configurar una zona que coincida con la usada para los días existentes.

Si un perfil heredado tiene `lastEvaluatedDate` de hoy (o futura), el primer cierre
permitido reconstruye current/best y la última fecha a partir de las instantáneas
**cerradas** existentes. Elimina así el incremento prematuro del día abierto, incluso
si era un récord. No modifica esas instantáneas ni reconstruye tareas históricas.
Si faltan instantáneas antiguas, no puede recuperar un récord que solo existiera en
el perfil. Conviene revisar la integridad del historial antes del despliegue.

Las instantáneas de días ya pasados se aceptan como fuente histórica de verdad.
Un estado incorrecto consolidado por la versión anterior no puede corregirse con
seguridad a partir de las tareas actuales. Los logros desbloqueados o recompensas
ya reclamadas tampoco se revocan automáticamente. Cualquier reparación histórica
requiere análisis y autorización separados. Este trabajo no accede a la base QA.

## Validación manual de GAME-048

Usar un entorno de validación autorizado y un usuario de prueba nuevo. No alterar
el reloj del servidor ni datos de QA para simular el cierre.

1. Configurar la zona y arrancar solo la versión nueva con el cierre habilitado.
2. Con una obligatoria activa, completar tres tareas distintas válidas, incluida
   esa obligatoria. Comprobar 3/3 y `dayQualified=true`; la racha cerrada no aumenta.
3. Crear desde una plantilla obligatoria activa “Spring Boot intensivo”. Tras F5,
   comprobar 3/3, obligatorias 1/2 y `dayQualified=false`; las rachas no cambian.
4. Completar otra opcional: aumenta el contador de válidas, pero no cualifica.
5. Completar la nueva obligatoria: vuelve a cualificar sin incrementar aún la racha.
6. Tras pasar la medianoche de la zona y ejecutar el cierre, comprobar incremento
   único, récord coherente y `lastEvaluatedDate` igual a la fecha que terminó.
7. Repetir `finalize-day` para esa fecha: 204 y los mismos valores. Solicitar hoy:
   400 `DAY_NOT_CLOSED`.
8. Repetir por creación manual y dejar la obligación pendiente hasta el cierre:
   `currentStreak=0`, récord conservado. Las nuevas tareas del siguiente día no
   cambian los contadores ni la cualificación de la fecha cerrada.

Los tests automatizados adelantan un Clock controlado en Testcontainers y cubren
ambas creaciones, opcionales/obligatorias posteriores, cierres, huecos, reintentos,
historial, transición heredada, contratos HTTP y concurrencia real con claims.

## Inventario de archivos afectados

Todos pertenecen al backend; frontend y security-core quedan sin cambios.

- `docs/DAILY_QUALIFICATION_AND_STREAKS.md`
- `docs/ROADMAP.md`
- `src/main/java/com/davidrr/grindprotocol/activity/service/impl/UserActivityEventServiceImpl.java`
- `src/main/java/com/davidrr/grindprotocol/common/exception/ErrorCodes.java`
- `src/main/java/com/davidrr/grindprotocol/progression/controller/ProgressionController.java`
- `src/main/java/com/davidrr/grindprotocol/progression/dto/ProgressionSummaryResponse.java`
- `src/main/java/com/davidrr/grindprotocol/progression/service/DayClosingJob.java`
- `src/main/java/com/davidrr/grindprotocol/progression/service/StreakService.java`
- `src/main/java/com/davidrr/grindprotocol/progression/service/impl/StreakServiceImpl.java`
- `src/main/java/com/davidrr/grindprotocol/progression/time/DayCalendar.java`
- `src/main/java/com/davidrr/grindprotocol/progression/time/DayConfiguration.java`
- `src/main/java/com/davidrr/grindprotocol/quest/service/impl/QuestEvaluationServiceImpl.java`
- `src/main/java/com/davidrr/grindprotocol/quest/service/impl/QuestServiceImpl.java`
- `src/main/java/com/davidrr/grindprotocol/task/controller/DailyProgressController.java`
- `src/main/java/com/davidrr/grindprotocol/task/dto/DailyProgressResponse.java`
- `src/main/java/com/davidrr/grindprotocol/task/repository/DailyProgressRepository.java`
- `src/main/java/com/davidrr/grindprotocol/task/service/impl/DailyProgressServiceImpl.java`
- `src/main/java/com/davidrr/grindprotocol/task/service/impl/TaskCompletionServiceImpl.java`
- `src/main/java/com/davidrr/grindprotocol/task/service/impl/TaskServiceImpl.java`
- `src/main/java/com/davidrr/grindprotocol/userprofile/dto/UserProfileResponse.java`
- `src/main/java/com/davidrr/grindprotocol/userprofile/repository/UserProfileRepository.java`
- `src/main/resources/application-integration-test.yaml`
- `src/main/resources/application-test.yaml`
- `src/main/resources/application.yaml`
- `src/test/java/com/davidrr/grindprotocol/activity/service/UserActivityEventServiceImplTest.java`
- `src/test/java/com/davidrr/grindprotocol/integration/ClaimConcurrencyIT.java`
- `src/test/java/com/davidrr/grindprotocol/integration/progression/StreakFlowIT.java`
- `src/test/java/com/davidrr/grindprotocol/progression/controller/ProgressionControllerTest.java`
- `src/test/java/com/davidrr/grindprotocol/progression/service/DayCalendarTest.java`
- `src/test/java/com/davidrr/grindprotocol/progression/service/DayClosingJobTest.java`
- `src/test/java/com/davidrr/grindprotocol/progression/service/StreakServiceImplTest.java`
- `src/test/java/com/davidrr/grindprotocol/quest/service/QuestEvaluationServiceImplTest.java`
- `src/test/java/com/davidrr/grindprotocol/quest/service/QuestServiceImplTest.java`
- `src/test/java/com/davidrr/grindprotocol/task/service/DailyProgressServiceImplTest.java`
- `src/test/java/com/davidrr/grindprotocol/task/service/TaskCompletionServiceImplTest.java`
- `src/test/java/com/davidrr/grindprotocol/task/service/TaskServiceImplTest.java`
