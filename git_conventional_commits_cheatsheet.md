# 🧠 Git + Conventional Commits Cheat Sheet (Grind Protocol)

---

# 🌿 Flujo de trabajo con ramas

## 🧱 Crear rama
```bash
git checkout main
git pull origin main
git checkout -b feature/nombre-funcionalidad
```

## 💾 Trabajar (commits frecuentes)
```bash
git add .
git commit -m "feat: add task entity"
git push -u origin feature/nombre-funcionalidad
```

## 🔁 Seguir trabajando
```bash
git add .
git commit -m "feat: add task service"
git push
```

## 🚀 Crear Pull Request (PR)
👉 Cuando el bloque esté completo

## ✅ Merge
👉 Esto dispara release-please

---

# 🔥 Reglas clave

- ✔ Commit → frecuente
- ✔ Push → frecuente
- ❌ PR por commit → NO
- ✔ PR = bloque funcional completo

---

# 🧠 Conventional Commits (adaptado)

## 🎯 FUNCIONALIDAD
```
feat: add task domain model
feat: implement daily streak logic
feat: add reward system
feat: add xp calculation engine
```

👉 Añade funcionalidad visible

---

## 🐛 BUGS
```
fix: correct streak reset bug
fix: prevent duplicate task completion
fix: fix xp calculation rounding
```

---

## 🧱 REFACTOR
```
refactor: extract reward service
refactor: simplify task validation logic
refactor: move domain logic to service layer
```

👉 No cambia comportamiento

---

## 📚 DOCUMENTACIÓN
```
docs: add roadmap
docs: update README
docs: document task rules
```

---

## 🧪 TESTS
```
test: add task service tests
test: add streak validation tests
```

---

## ⚙️ CI / AUTOMATIZACIÓN
```
ci: add release-please workflow
ci: update github actions config
```

---

## 🧹 MANTENIMIENTO
```
chore: update dependencies
chore: add gitignore
chore: format code
```

---

# 💥 BREAKING CHANGES

## Opción 1
```
feat!: change reward system behavior
```

## Opción 2
```
feat: change reward system

BREAKING CHANGE: rewards now require level
```

👉 Esto sube versión mayor

---

# 🔥 Ejemplo real de flujo

```
feat: add task entity
push

feat: add task repository
push

feat: implement task completion
push

fix: prevent duplicate completion
push

docs: update roadmap
push
```

---

# 🚀 Regla de oro

👉 "Si tiene sentido como bloque → PR"

👉 "Si es pequeño → commit"
