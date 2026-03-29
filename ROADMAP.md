# 🎮 Grind Protocol — Definición completa del proyecto

## 1. 🧠 Visión del producto
**Grind Protocol** es una aplicación de gamificación personal que convierte la gestión de tareas, hábitos y objetivos en un sistema de progresión tipo RPG.

La idea central del producto es que el usuario no solo marque tareas como completadas, sino que avance dentro de un sistema con reglas claras, progreso visible y recompensas reales. Cada tarea completada aporta experiencia, hace crecer estadísticas personales, ayuda a mantener rachas y genera una moneda interna que puede canjearse por recompensas definidas por el propio usuario o por plantillas preconfiguradas del sistema.

Grind Protocol no pretende ser simplemente una to-do app ni un habit tracker clásico. Su objetivo es comportarse como un motor de disciplina y progresión personal, donde la constancia tenga valor, el incumplimiento tenga consecuencias y el esfuerzo diario se traduzca en resultados tangibles.

---

## 2. 🎯 Objetivos principales
El producto busca:

- ✅ fomentar la constancia diaria
- ⏳ reducir la procrastinación
- 📈 hacer visible el progreso personal
- 🏆 premiar el esfuerzo sostenido
- 🔄 transformar hábitos y tareas en un sistema motivador
- 🎮 ofrecer una experiencia más cercana a un juego de progreso que a una herramienta de productividad tradicional

Además, el proyecto está pensado para poder evolucionar en el futuro hacia una capa social con comparativas, rivalidades, retos y duelos entre usuarios.

---

## 3. 🧬 Identidad del producto
Grind Protocol se apoya en una identidad de producto basada en cuatro ideas:

- 🛡️ disciplina
- 📈 progreso
- 💱 economía de esfuerzo
- ⚡ feedback constante

El usuario progresa porque actúa, no porque planifica. El sistema premia hacer, mantener y sostener.

---

## 4. 🧩 Núcleo funcional del sistema

### 4.1 📋 Sistema de tareas
Las tareas son la unidad principal de acción del producto. Representan misiones, hábitos, responsabilidades u objetivos.

El sistema debe permitir crear y gestionar distintos tipos de tareas:

- 📅 diarias
- 🗓️ semanales
- 📍 puntuales
- 🛤️ objetivos largos o de largo recorrido

Cada tarea puede incluir, entre otros, estos atributos funcionales:

- 🏷️ nombre
- 📝 descripción opcional
- 🗂️ categoría
- 🎚️ dificultad
- 🔁 frecuencia
- ⭐ experiencia base
- ⏰ hora o límite de caducidad
- 🔥 si cuenta para racha
- ❗ si es obligatoria
- 🔢 cuántas veces puede completarse en un día
- 📉 si tiene rendimientos decrecientes
- 🧬 qué estadísticas afecta

Las tareas diarias vencen por defecto a las 23:59 en la hora local del usuario.  
Las tareas semanales cierran el domingo a las 23:59.

---

### 4.2 🗂️ Categorías de tareas
El sistema tendrá unas categorías base iniciales:

- 🧠 mente
- 💪 cuerpo
- 💼 trabajo
- 🌱 personal

Estas categorías no deben considerarse cerradas. Deben quedar definidas como un sistema extensible, para poder ampliar, personalizar o reorganizar categorías en versiones futuras sin romper el diseño general del producto.

---

### 4.3 🎚️ Dificultad de tareas
La dificultad no debe ser completamente libre, porque eso facilitaría trampas y rompería comparativas futuras entre usuarios.

La propuesta consolidada es que la dificultad esté controlada por el sistema, pero dentro de un rango seleccionable por el usuario. Es decir, el usuario puede elegir una dificultad entre opciones acotadas, pero la experiencia final no la inventa él libremente, sino que la calcula el sistema en base a reglas predefinidas.

Esto permite personalización sin perder consistencia.

---

## 5. ✅ Sistema de completado de tareas
Una cosa es la definición de una tarea y otra su ejecución. El sistema debe registrar cada completado como un evento independiente.

Cada vez que el usuario completa una tarea, debe quedar constancia de:

- 📌 qué tarea fue
- 🕒 cuándo se completó
- 🔁 si era la primera vez del día o una repetición
- ⭐ cuánta experiencia otorgó realmente
- 🪙 cuánta moneda interna generó
- 📊 si aportó al mínimo diario
- 🔥 si afectó a la racha
- 🧬 si modificó estadísticas
- ⚖️ qué bonus o penalizaciones aplicaron
- ⚙️ si activó o no reglas especiales

Esto es clave para mantener histórico real, analítica fiable e integridad del sistema.

---

## 6. 🔁 Repetición de tareas en el mismo día
El sistema debe permitir que algunas tareas puedan completarse varias veces al día, pero con reglas de balance.

Cada tarea podrá definir:

- 🔄 si puede repetirse en el mismo día
- 🔢 cuántas veces como máximo
- 📉 si aplica rendimientos decrecientes

La regla consolidada es esta:

- la primera vez que se completa una tarea en el día:
  - ⭐ da la experiencia completa
  - ✅ puede contar para el mínimo diario
  - 🔥 puede contar para racha

- las repeticiones posteriores:
  - 📉 dan experiencia reducida
  - 🚫 no cuentan como tareas nuevas para cumplir el día
  - 🚫 no cuentan para racha

Con esto se evita el spam de tareas fáciles, pero se permite flexibilidad para tareas básicas o repetibles.

---

## 7. 📆 Cumplimiento diario
El sistema necesita definir qué significa “cumplir el día”, ya que esto afecta directamente a las rachas y al progreso.

La decisión cerrada es que el día se considera cumplido cuando se alcanzan dos condiciones:

- ✅ se completa el número mínimo de tareas válidas
- ❗ se completan todas las tareas obligatorias del día

El valor por defecto para el mínimo diario será 3 tareas, pero el sistema debe permitir que el usuario lo personalice dentro de unos límites razonables. No debe ser completamente libre sin restricciones, para evitar romper el equilibrio del sistema.

Las tareas repetidas en el mismo día no cuentan como tareas adicionales para ese mínimo.

---

## 8. ❗ Tareas obligatorias
El sistema debe permitir marcar tareas como obligatorias.

Estas tareas tienen un peso especial en el cumplimiento diario. No basta con completar cualquier combinación de tareas si faltan las obligatorias.

Esto añade profundidad estratégica y refuerza la idea de disciplina real. También permite que el usuario pueda adaptar qué considera verdaderamente importante en su sistema personal.

En el futuro, una tarea obligatoria podrá dejar de serlo si el usuario decide modificarla.

---

## 9. ⭐ Sistema de experiencia (XP)
La experiencia representa el progreso global del usuario.

La XP:

- 🎯 se gana completando tareas
- 🔒 no se gasta
- 📚 se acumula históricamente
- 🆙 sirve para subir de nivel

La experiencia final de una tarea no tiene por qué coincidir siempre con la experiencia base, ya que pueden intervenir factores como:

- 🎚️ dificultad
- 🔁 repetición en el mismo día
- 💀 debuffs
- ✨ bonus futuros
- 🔥 efectos de racha o sistemas avanzados

En la primera versión, el sistema de XP debe ser claro, previsible y suficientemente estable como para servir de base a futuras comparativas sociales.

---

## 10. 📈 Sistema de niveles
El nivel representa la progresión global del usuario dentro de Grind Protocol.

Subir de nivel debe depender de la XP acumulada histórica. El nivel nunca debe bajar por gastar recompensas, porque la moneda y la experiencia son sistemas distintos.

El nivel servirá para:

- 🧭 representar el progreso del usuario
- 🏅 marcar hitos
- 🔓 desbloquear acceso a ciertas recompensas
- 🧠 apoyar sistemas futuros como títulos, perks, builds o rangos

La curva de nivel debe ser creciente, de forma que subir los primeros niveles sea accesible y luego la progresión gane peso.

---

## 11. 🪙 Sistema de moneda interna
El producto separa claramente experiencia y moneda canjeable.

La experiencia sirve para progresión.  
La moneda sirve para recompensas.  
El nombre de la moneda es **Core Points**.

La regla consolidada actual es:

**100 XP = 10 Core Points**

Esa relación puede ajustarse a futuro, pero servirá como base inicial de balance.

La moneda podrá utilizarse para:

- 🎁 desbloquear recompensas
- 🛒 canjear recompensas personalizadas
- 🛡️ en el futuro, comprar salvadores de racha u otros consumibles del sistema

---

## 12. 🏆 Sistema de recompensas
Las recompensas son el mecanismo de pago emocional del producto. Convierten el esfuerzo en permiso para obtener algo deseado.

El sistema de recompensas será mixto:

- ✍️ recompensas creadas libremente por el usuario
- 🧩 recompensas predefinidas mediante plantillas del sistema

Ejemplos de recompensas:

- 🎮 comprar un juego
- 📚 comprar un libro
- 🍔 pedir comida
- 🎬 ver una película
- 🌤️ tarde libre
- ✨ capricho pequeño
- 🛌 descanso especial

Cada recompensa puede tener:

- 🏷️ nombre
- 📝 descripción
- 🪙 coste en Core Points
- 🔒 nivel mínimo requerido
- 🧭 tipo de disponibilidad
- ⏳ cooldown
- 📆 límite por periodo

---

## 13. ⏳ Disponibilidad de recompensas
Las recompensas no deben comportarse todas igual. Para dar profundidad al producto, cada recompensa debe poder tener una política de disponibilidad distinta.

Tipos contemplados:

- ♾️ ilimitada
- ⏳ con cooldown
- 📆 limitada por periodo

Por ejemplo:

- una recompensa pequeña puede ser ilimitada
- una recompensa media puede tener cooldown
- una recompensa importante puede estar limitada a una vez por semana o por mes

El uso de límites semanales o mensuales para ciertas recompensas se considera una buena práctica para evitar abusos y reforzar la sensación de valor.

---

## 14. 🧬 Sistema de estadísticas (stats)
El usuario tendrá un conjunto de estadísticas personales que representan su “build” o perfil de progreso.

Las estadísticas base iniciales propuestas son:

- 🛡️ disciplina
- 🧠 inteligencia
- 💪 fuerza
- ⚡ energía
- 💻 skill técnica

Estas estadísticas tampoco deben considerarse cerradas de forma rígida. El sistema debe quedar preparado para ampliarlas o modificarlas en el futuro.

En la primera versión, las stats serán principalmente visuales y de identidad de personaje. Es decir, subirán con tareas concretas y se mostrarán en el perfil, pero no afectarán aún al cálculo de XP.

A futuro sí podrán influir en:

- ✖️ multiplicadores
- 🔓 desbloqueos
- 🧭 especializaciones
- 🎁 perks
- 🏗️ builds

---

## 15. 🔥 Sistema de rachas
Las rachas son una parte central de la motivación del producto.

La primera versión debe centrarse en la racha global diaria.  
Esa racha aumenta si el usuario cumple el día y se rompe si no lo hace.

A futuro, el sistema podrá incorporar también:

- 🔁 rachas por tarea
- 🗂️ rachas por categoría

La protección de racha no existirá desde el inicio, pero sí está prevista como mecánica futura.

---

## 16. 🛡️ Salvadores o protección de racha
No habrá descanso gratuito ni protección de racha automática en la primera versión.

La visión futura es que un salvador de racha o protección pueda:

- 🏅 ganarse por tiempo o hitos
- ✅ conseguirse por méritos
- 🪙 comprarse con moneda interna

Esto da más valor a la racha, evita que el sistema sea indulgente en exceso y encaja bien con la filosofía de disciplina del producto.

---

## 17. 🚫 Ausencia de días de descanso
El sistema no contemplará días de descanso como mecánica estándar.

La decisión tomada es que, si el usuario no alcanza el mínimo diario o no cumple sus tareas obligatorias:

- 💥 pierde la racha
- 💀 y puede sufrir un debuff

Esto hace que Grind Protocol sea un sistema exigente y coherente con su planteamiento. Si el usuario quiere protegerse, deberá usar recursos futuros del propio sistema, no una pausa gratuita.

---

## 18. 💀 Sistema de debuffs
El producto debe contemplar debuffs como consecuencia de fallar un día.

No tienen por qué ser extremadamente duros, pero sí visibles y significativos. Su función es reforzar el valor del cumplimiento y dar peso a la pérdida de racha.

Ejemplos de debuff posibles:

- 📉 reducción temporal de XP
- 🪙 reducción temporal de moneda ganada
- 🔒 bloqueo parcial de ciertas recompensas
- ⏱️ penalización durante un tiempo limitado

La idea es que el sistema tenga consecuencias, no solo ausencia de premio.

---

## 19. 🚨 Sistema de alertas
El sistema de alertas forma parte del núcleo del producto y no debe verse como un simple recordatorio genérico.

Su objetivo es detectar riesgo, oportunidad y progreso cercano.

### Alertas de riesgo
- 🔥 la racha está en peligro
- ⏳ falta poco para cumplir el día
- ❗ aún no has hecho una tarea obligatoria

### Alertas de caducidad
- ⌛ una tarea va a expirar
- 📅 una tarea semanal está a punto de cerrar

### Alertas de progreso
- 🆙 estás cerca de subir de nivel
- 🪙 estás a poca moneda de una recompensa

### Alertas de oportunidad
- 🛟 una acción concreta puede salvar el día
- ⚡ una tarea concreta te acerca a una mejora inmediata

Esto convierte la aplicación en un sistema proactivo, no solo reactivo.

---

## 20. 🕰️ Evaluación del día
La evaluación del día se realizará con un modelo híbrido.

- 👀 en tiempo real, el sistema mostrará el progreso parcial del día
- ✅ al cierre del día, a las 23:59 del usuario, se hará la validación definitiva

Así el usuario puede ver en todo momento cómo va, pero la decisión final sobre racha, cumplimiento y debuffs se toma al cierre del día.

---

## 21. 🧾 Integridad de datos e histórico
El sistema no debe permitir eliminar completados históricos.

La recomendación consolidada es:

- 🚫 no borrar tareas completadas
- 📴 permitir desactivar tareas
- ✏️ permitir editar tareas solo hacia futuro
- 🧱 preservar el histórico real

Esto protege:

- 🛡️ la integridad del sistema
- 📊 la analítica
- 📈 las estadísticas
- 🔍 la auditoría
- 🚫 la prevención de trampas

Borrar históricos rompería el sentido del producto.

---

## 22. 🕵️ Sistema anti-cheat
Aunque no sea una feature “visible” de cara al usuario, debe ser un principio de diseño del proyecto.

Algunas líneas importantes:

- 🚫 evitar XP arbitraria
- 🚫 evitar inflar dificultad libremente
- 🔁 controlar repeticiones
- ❌ impedir que una tarea repetida varias veces cuente como si fueran tareas distintas
- ⚖️ mantener consistencia para posibles comparativas futuras

Esto es especialmente importante porque Grind Protocol quiere dejar abierta la puerta a duelos y rankings entre usuarios.

---

## 23. 🕓 Historial y timeline
El producto debe incluir un sistema de historial o timeline de actividad.

Ese histórico puede registrar eventos como:

- ✅ tarea completada
- 🆙 nivel subido
- 🔥 racha mantenida
- 💥 racha perdida
- 🎁 recompensa canjeada
- 💀 debuff aplicado
- 🏅 hito alcanzado

Esto aporta valor tanto funcional como visual y es una de las partes que más puede hacer que el proyecto destaque.

---

## 24. ⚔️ Capa social futura
La primera versión no debe centrarse en lo social, pero sí dejarlo previsto desde el diseño.

La visión futura contempla:

- 🌐 perfil público parcial
- 📊 comparación de estadísticas
- 🤝 rivalidades amistosas
- 🎯 retos
- ⚔️ duelos
- 🏅 rankings limitados

No se pretende convertir Grind Protocol en una red social, sino añadir una capa opcional de competencia sana y comparación motivadora.

Para que eso funcione bien, el sistema base debe ser justo, consistente y difícil de manipular.

---

## 25. 🔐 Perfiles públicos y privacidad futura
Si la capa social se activa más adelante, no toda la información del usuario debería ser visible.

La idea sería compartir solo ciertos elementos:

- 🆙 nivel
- 📈 progreso general
- 🔥 racha actual
- 🧬 algunas estadísticas
- 🏅 logros destacados

Mientras que otros datos deberían seguir siendo privados:

- 📝 tareas concretas
- 🎁 recompensas personales
- 🕓 histórico detallado

Esto también debe estar presente en el diseño desde el inicio, aunque no se implemente aún.

---

## 26. 🧩 Recompensas predefinidas y personalizadas
El sistema debe incluir una base de recompensas sugeridas o plantillas, para facilitar la experiencia inicial del usuario.

Ejemplos de plantillas:

- 🍫 snack
- 🍔 comida especial
- 📚 comprar libro
- 🎮 comprar juego
- 🛌 descanso especial
- 🌤️ tarde libre
- ✨ ocio premium

Además, el usuario podrá crear recompensas totalmente personalizadas para adaptar el sistema a su vida real.

---

## 27. 🏅 Logros y achievements futuros
Aunque no sean estrictamente prioritarios para la primera versión, los logros se consideran una funcionalidad con mucho potencial.

Ejemplos:

- 🗓️ primera semana perfecta
- 🆙 nivel 5
- 🔥 7 días seguidos
- 🧠 30 tareas de mente
- ✅ 50 tareas completadas
- 📈 récord personal de racha

Esto encaja perfectamente con la identidad del producto y mejora mucho la sensación de progreso.

---

## 28. 🎨 Diseño visual y experiencia
El estilo visual definido para Grind Protocol no debe ser extremadamente futurista. La referencia correcta es un enfoque de **dark gamified minimal**.

Características del estilo:

- 🌑 fondo oscuro elegante
- 💖 acentos magenta o rosa intenso
- ✨ glow sutil y controlado
- 🧱 tarjetas limpias
- 📐 jerarquía visual clara
- 🎮 estética moderna con energía gaming, pero sin caer en un estilo sci-fi exagerado

No se busca una interfaz cyberpunk extrema ni un diseño de streamer. La dirección correcta es un dashboard premium, limpio, con identidad de juego pero sobrio.

---

## 29. 🎨 Paleta visual orientativa
La dirección visual ideal sería algo así:

- ⚫ fondo negro o gris muy oscuro
- 💖 color principal magenta/fucsia
- 🔵 color secundario para progreso y XP en tonos cian/azul
- 🟠 color para racha en tonos cálidos, como naranja
- 🟡 color para recompensas en dorado o amarillo acentuado

La clave es que el glow no se use en exceso, sino como refuerzo de elementos importantes.

---

## 30. 🧭 Principios UX del producto
La experiencia debe apoyarse en estos principios:

- 👀 el progreso debe verse siempre
- ⚡ el feedback debe ser inmediato
- 🧼 la interfaz debe ser clara y no recargada
- 🔔 las alertas deben ser útiles, no molestas
- 🔥 la racha debe sentirse valiosa
- 🎁 las recompensas deben percibirse como ganadas
- 💥 el incumplimiento debe tener consecuencias visibles

---

## 31. 🔁 Loop diario del producto
El loop principal de Grind Protocol queda definido así:

1. 🚪 el usuario entra en la app
2. 📋 ve sus tareas del día
3. ✅ completa tareas
4. ⭐ gana XP y moneda
5. 🔥 mantiene o mejora su racha
6. 📈 observa progreso inmediato
7. 🎁 se acerca a recompensas reales
8. ⏰ intenta cerrar el día correctamente antes del límite

Ese loop es el corazón del producto.

---

## 32. 🚫 Qué no es Grind Protocol
Para dejarlo bien definido, Grind Protocol no quiere ser:

- 📝 una lista de tareas minimalista sin reglas
- 📒 una agenda clásica
- 🗒️ una app de notas
- 📉 un habit tracker plano sin consecuencias
- 🌐 una red social generalista

Su identidad es mucho más cercana a un sistema de progreso personal estructurado.

---

## 33. 🚀 Funcionalidades diferenciales con más potencial
Las funcionalidades que más pueden hacer destacar este proyecto son:

- ⚖️ sistema de XP y moneda separadas
- 🔥 rachas con consecuencias reales
- ❗ tareas obligatorias
- 📉 rendimientos decrecientes en repeticiones
- 🚨 sistema de alertas inteligente
- 🕓 timeline de eventos
- 💀 debuffs
- 🛒 tienda de recompensas seria
- 🧬 estadísticas visuales del personaje
- ⚔️ base lista para futuros retos, comparativas y duelos

---

## 34. ✅ Estado del producto antes de pasar a diseño técnico
Con todo lo definido hasta aquí, Grind Protocol ya tiene cerrados:

- 🧠 visión
- 🧬 identidad
- 🎮 reglas del juego
- 💱 economía del sistema
- 📈 progresión
- 🔥 rachas
- 🧬 estadísticas
- 🏆 recompensas
- 🚨 alertas
- 💀 consecuencias de fallo
- 🎨 enfoque visual
- ⚔️ expansión futura social

Esto ya constituye una base de producto muy sólida y suficientemente definida para pasar a la siguiente fase.
