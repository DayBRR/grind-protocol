# 🧱 ARCHITECTURE.md

# Grind Protocol — Backend Architecture

## 1. Purpose

This document defines the initial backend architecture for **Grind Protocol**.

The goal is to build a backend-first application with:

- clear business rules
- modular structure
- scalable domain boundaries
- clean separation of concerns
- a workflow ready for future growth

Grind Protocol is not just a CRUD app.  
It is a **gamified personal progression platform** with tasks, streaks, XP, Core Points, rewards, alerts, and future social mechanics.

---

## 2. Architecture style

The project will follow a **clean and modular backend architecture**, using a pragmatic approach inspired by:

- Clean Architecture
- Domain-oriented design
- Feature-based package organization

The objective is to keep business rules independent from infrastructure and easy to evolve over time.

---

## 3. High-level module boundaries

The backend is divided into these logical modules:

- `user`
- `task`
- `progression`
- `streak`
- `reward`
- `stat`
- `alert`
- `timeline`
- `common`
- `config`

Future modules:

- `social`
- `challenge`
- `achievement`

---

## 4. Package root

The base package will be:

```java
com.davidrr.grindprotocol
```

---

## 5. Internal structure per module

Each main module should follow a similar internal structure:

```text
module
├── domain
│   ├── model
│   ├── repository
│   ├── service
│   └── enum
├── application
│   ├── usecase
│   ├── dto
│   └── mapper
├── infrastructure
│   ├── persistence
│   │   ├── entity
│   │   ├── jpa
│   │   └── adapter
└── interfaces
    └── rest
        ├── controller
        ├── request
        └── response
```

This structure keeps the project organized by feature, while still separating domain, use cases, persistence, and API entry points.

---

## 6. Layer responsibilities

### Domain
Contains the business core:

- domain models
- enums
- repository contracts
- domain services
- business rules

This layer must not depend directly on Spring MVC or JPA details.

### Application
Contains the use cases of the system:

- create task
- complete task
- evaluate daily progress
- claim reward
- generate alerts

This layer coordinates domain logic and repository usage.

### Infrastructure
Contains technical implementations:

- JPA entities
- Spring Data repositories
- persistence adapters
- external integrations

### Interfaces
Contains entry points:

- REST controllers
- request/response models
- input validation

---

## 7. Initial MVP domain focus

The MVP should focus on these core areas first:

### Phase 1
- user profile
- task
- task completion
- daily progress

### Phase 2
- XP calculation
- level calculation
- Core Points
- daily streak

### Phase 3
- rewards
- timeline events
- basic alerts

Not included in initial MVP:

- social features
- duels
- rankings
- achievements
- stats affecting XP
- streak protection
- advanced debuffs

---

## 8. Main domain entities (MVP)

### User
Authentication identity and basic account data.

### UserProfile
Game profile of the user:

- total XP
- Core Points
- daily goal
- current streak
- best streak
- level-related progress data

### Task
Definition of a task or mission.

### TaskCompletion
A real completion event of a task.

### DailyProgress
Daily summary used to validate whether the day qualifies.

### Reward
Definition of a reward.

### RewardClaim
Actual reward redemption event.

### Alert
User-facing alert or notification.

### TimelineEvent
Historic event for traceability and UX.

---

## 9. Key architectural principles

### 1. Business rules first
The core logic must live in services and models, not in controllers.

### 2. No anemic design if avoidable
Where reasonable, domain objects and domain services should express rules clearly.

### 3. Persistence should not define the domain
JPA entities are implementation details, not the business model itself.

### 4. Explicit use cases
Each relevant action should have a dedicated application use case.

### 5. Feature-first packaging
Packages should follow product capabilities, not framework categories.

### 6. Future-proof domain boundaries
Even if some modules are not implemented yet, the structure should allow their future addition.

---

## 10. Naming conventions

### Use cases
- `CreateTaskUseCase`
- `CompleteTaskUseCase`
- `EvaluateDailyProgressUseCase`
- `ClaimRewardUseCase`

### Services
- `TaskCompletionService`
- `DailyProgressEvaluator`
- `XpCalculationService`
- `CorePointsCalculationService`
- `StreakService`
- `RewardClaimService`
- `AlertGenerationService`
- `TimelineEventService`

### Repository contracts
- `TaskRepository`
- `TaskCompletionRepository`
- `DailyProgressRepository`
- `RewardRepository`

### Persistence implementations
- `JpaTaskRepository`
- `JpaTaskCompletionRepository`

---

## 11. REST API philosophy

The API should be designed around clear use cases, not generic database exposure.

Examples:

- `POST /api/tasks`
- `GET /api/tasks/today`
- `POST /api/tasks/{taskId}/complete`
- `GET /api/progression/dashboard`
- `POST /api/rewards/{rewardId}/claim`

The backend should expose meaningful actions aligned with the product experience.

---

## 12. Testing strategy

The project should include:

### Unit tests
For:
- calculation logic
- validation rules
- daily qualification
- XP and Core Points calculation

### Integration tests
For:
- REST endpoints
- persistence behavior
- use case flows

Recommended command for CI:

```bash
mvn clean verify
```

---

## 13. CI workflow policy

Every Pull Request to `main` should trigger:

- checkout
- Java 17 setup
- Maven dependency cache
- `mvn clean verify`

This ensures that only valid changes are merged into the stable branch.

---

## 14. Branching policy

The project will use:

- `main` as stable branch
- short-lived branches for work

Branch naming examples:

- `feature/task-domain`
- `feature/task-completion`
- `fix/streak-validation`
- `docs/update-roadmap`
- `refactor/reward-service`
- `ci/pull-request-checks`

No direct development should happen on `main`.

---

## 15. Versioning and releases

The project uses:

- Semantic Versioning
- Conventional Commits
- release-please
- automated GitHub Releases
- automated CHANGELOG generation

This means:

- developers do not create tags manually
- releases are generated after merging the Release PR
- commit discipline matters

---

## 16. Relationship with portfolio

Grind Protocol should be presented as:

> A backend-first gamified personal progression platform with clear domain modeling, automated releases, and a structured development workflow.

This architecture is part of the project value and should be reflected in the repository and later in the portfolio.

---

## 17. Next architectural milestone

After this base, the next step is to define:

- exact MVP entities
- relationships
- package skeleton
- first use cases
- initial REST endpoints
