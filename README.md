# 🎮 Grind Protocol

![Release](https://img.shields.io/github/v/release/DayBRR/grind-protocol)
![Build](https://img.shields.io/github/actions/workflow/status/DayBRR/grind-protocol/release-please.yml)
![License](https://img.shields.io/badge/license-MIT-blue)
![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)

---
## 📚 Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Git conventional commits](docs/git_conventional_commits_cheatsheet.md)
- [Database Design](docs/DATABASE_DESIGN.md)
- [Roadmap](docs/ROADMAP.md)
- [Contributing](docs/CONTRIBUTING.md)
---

## 🧠 What is Grind Protocol?

**Grind Protocol** is a personal gamification system that transforms your daily tasks into an RPG-style progression system.

Instead of just completing tasks, you:

- ⭐ Earn XP
- 🔥 Maintain streaks
- 🆙 Level up
- 🪙 Earn **Core Points**
- 🎁 Unlock real-life rewards

👉 This is not a to-do app.  
👉 This is a **discipline engine**.

---

## 🎯 Core Philosophy

- 🛡️ Discipline over motivation
- 📈 Progress must be visible
- ⚖️ Effort must be rewarded
- 💀 Failure must have consequences
- 🔁 Consistency is king

---

## ⚙️ Core Systems

### 📋 Tasks System
- Daily / Weekly / One-time / Long-term tasks
- Mandatory tasks support
- Controlled difficulty
- Repeatable tasks with diminishing returns

---

### ⭐ XP & 🆙 Levels
- XP is earned by completing tasks
- Levels represent long-term progression
- Progressive difficulty curve

---

### 🪙 Core Points (Currency)
- 💱 100 XP = 10 Core Points
- Used to unlock rewards
- Separate from XP (no progression loss)

---

### 🎁 Rewards System
- Custom rewards (user-defined)
- Predefined templates
- Cooldowns and limits

Examples:
- 🎮 Buy a game
- 📚 Buy a book
- 🍔 Order food
- 🎬 Watch a movie

---

### 🔥 Streak System
- Daily streak based on task completion
- Broken if daily requirements are not met
- No free rest days

---

### 💀 Debuffs
Failing a day can result in:

- XP reduction
- Currency penalty
- Temporary restrictions

---

### 🚨 Smart Alerts
- 🔥 Streak at risk
- ⏳ Tasks about to expire
- 🆙 Level almost reached
- 🪙 Reward unlocked

---

### 🧬 Stats System (Future-ready)
- Discipline
- Intelligence
- Strength
- Energy
- Technical Skill

Currently visual → future impact on gameplay

---

## 🔁 Daily Loop

1. Open the app
2. Complete tasks
3. Earn XP & Core Points
4. Maintain streak
5. Progress towards rewards

---

## 🚀 Roadmap

See full product definition here:

👉 [ROADMAP.md](./ROADMAP.md)

---

## 🧪 Versioning & Releases

This project uses:

- 🏷️ **Semantic Versioning**
- 🤖 **release-please (Google)**
- 📦 Automated changelog & releases

### Commit Convention

This project follows **Conventional Commits** to maintain a clear and structured commit history and to enable automated releases.

Commit messages follow this format:

`<type>(optional scope): description`

Examples:

```text
feat(portfolio): add project filtering
fix(ui): improve card spacing on mobile
docs(readme): update project documentation
refactor(components): simplify project card logic
chore(ci): update GitHub Actions workflow
```
### Common Commit Types

| Type     | Description                                |
| -------- | ------------------------------------------ |
| feat     | Introduces a new feature                   |
| fix      | Fixes a bug                                |
| docs     | Documentation changes                      |
| refactor | Code refactoring without changing behavior |
| chore    | Maintenance tasks                          |
| ci       | Continuous integration changes             |

---

## 🧱 Tech Stack

- ☕ Java 17+
- 🌱 Spring Boot 3.x
- 🔐 Modular security (security-core)
- 🧪 JUnit 5
- 📄 OpenAPI (Springdoc)
- 🗄️ H2 / PostgreSQL (future)

---

## 📌 Project Status

🚧 In active development (MVP phase)

---

## 👤 Author

**David Ruiz**

---

## 📄 License

MIT License
