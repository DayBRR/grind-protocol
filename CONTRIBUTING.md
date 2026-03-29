# 🤝 Contributing Guide — Grind Protocol

Este documento define cómo trabajar en el proyecto para mantener un flujo limpio, escalable y profesional.

---

# 🌳 Estrategia de ramas

- `main` → rama estable (producción)
- `feature/*` → nuevas funcionalidades
- `fix/*` → corrección de bugs
- `ci/*` → cambios de CI/CD

⚠️ **Nunca trabajar directamente sobre `main`**

---

# 🚀 Flujo de trabajo

## 1. Crear una rama

```bash
git checkout -b feature/nombre-feature
```

---

## 2. Commits (Conventional Commits)

Ejemplos:

```bash
feat: add task entity
fix: prevent duplicate completion
refactor: extract reward service
docs: update roadmap
```

---

## 3. Push de la rama

```bash
git push origin feature/nombre-feature
```

---

## 4. Crear Pull Request

- Base: `main`
- Título claro (ej: `feat: task domain implementation`)
- Descripción explicando qué incluye

---

## 5. Checks automáticos

El PR ejecuta:

- build (`mvn clean verify`)
- tests

👉 El PR debe pasar los checks antes de hacer merge

---

## 6. Merge

- Solo cuando todo esté OK
- Preferible: **Squash & Merge** o **Merge commit**

---

# 🧹 Limpieza de ramas

Después de mergear un PR:

## 1. Actualizar main

```bash
git checkout main
git pull origin main
```

---

## 2. Borrar rama local

```bash
git branch -d <branch-name>
```

---

## 3. Borrar rama remota (si no se borró automáticamente)

```bash
git push origin --delete <branch-name>
```

---

## 4. Limpiar referencias remotas

```bash
git fetch --prune
```

---

# 🧠 Buenas prácticas

- PRs pequeños y claros
- Un objetivo por rama
- No mezclar cambios de distinta naturaleza
- Mantener commits limpios y semánticos
- El código debe compilar siempre

---

# 🔖 Versionado

El proyecto usa:

👉 **Conventional Commits + release-please**

Esto permite:

- versionado automático
- generación de changelog
- releases automáticas

---

# 🧩 Filosofía del proyecto

Este proyecto busca:

- código limpio
- arquitectura clara
- modularidad (especialmente con `security-core`)
- enfoque backend profesional

---

🔥 Si tienes dudas, abre un issue o PR.
