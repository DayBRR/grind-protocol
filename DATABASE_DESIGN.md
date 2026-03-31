# 🧠 Database Design — Grind Protocol

This document defines the MVP database design for the Grind Protocol application.

---

## 📊 ER Diagram (MVP)

```mermaid
erDiagram
    USERS ||--|| USER_PROFILES : has
    USERS ||--o{ TASKS : owns
    USERS ||--o{ TASK_COMPLETIONS : performs
    USERS ||--o{ DAILY_PROGRESS : tracks
    USERS ||--o{ REWARDS : creates
    USERS ||--o{ REWARD_CLAIMS : redeems
    USERS ||--o{ ALERTS : receives
    USERS ||--o{ TIMELINE_EVENTS : generates

    TASKS ||--o{ TASK_COMPLETIONS : completed_as
    REWARDS ||--o{ REWARD_CLAIMS : claimed_as
```

---

## 🧱 Core Entities

### USERS
Represents the base identity of a user.

- id (PK)
- username (unique)
- email (unique)
- timezone
- active
- created_at
- updated_at

---

### USER_PROFILES
Represents the game state of the user.

- id (PK)
- user_id (FK, unique)
- display_name
- daily_task_goal
- total_xp
- core_points
- current_streak
- best_streak
- last_evaluated_date
- created_at
- updated_at

---

### TASKS
Defines user tasks.

- id (PK)
- user_id (FK)
- title
- description
- category
- difficulty
- task_type
- mandatory
- streak_eligible
- repeatable
- max_completions_per_day
- diminishing_returns_enabled
- active
- due_time
- weekly_closing_day
- created_at
- updated_at

---

### TASK_COMPLETIONS
Represents each task execution.

- id (PK)
- task_id (FK)
- user_id (FK)
- completed_at
- completion_date
- completion_index_for_day
- counted_for_daily_goal
- counted_for_streak
- base_xp
- awarded_xp
- awarded_core_points
- notes
- created_at

---

### DAILY_PROGRESS
Stores daily evaluation results.

- id (PK)
- user_id (FK)
- progress_date
- required_task_count
- completed_valid_task_count
- mandatory_tasks_required
- mandatory_tasks_completed
- day_qualified
- evaluated_at
- created_at

---

### REWARDS
Defines rewards.

- id (PK)
- user_id (FK, nullable)
- template_based
- title
- description
- cost_core_points
- required_level
- availability_type
- cooldown_days
- max_claims_per_period
- period_type
- active
- created_at
- updated_at

---

### REWARD_CLAIMS
Represents reward redemption.

- id (PK)
- reward_id (FK)
- user_id (FK)
- claimed_at
- cost_paid
- notes
- created_at

---

### ALERTS
Stores system alerts.

- id (PK)
- user_id (FK)
- alert_type
- severity
- title
- message
- related_entity_type
- related_entity_id
- triggered_at
- read_at
- dismissed_at
- active
- created_at

---

### TIMELINE_EVENTS
Stores historical events.

- id (PK)
- user_id (FK)
- event_type
- title
- description
- source_type
- source_id
- metadata_json
- created_at

---

## 🔗 Relationships

- Users → 1:1 → User Profiles
- Users → 1:N → Tasks
- Users → 1:N → Task Completions
- Users → 1:N → Daily Progress
- Users → 1:N → Reward Claims
- Users → 1:N → Alerts
- Users → 1:N → Timeline Events
- Tasks → 1:N → Task Completions
- Rewards → 1:N → Reward Claims

---

## 🧠 Design Decisions

- Separation between `users` and `user_profiles`
- `task_completions` is the source of truth
- `daily_progress` simplifies streaks and analytics
- `rewards.user_id` nullable for template system
- Alerts and timeline are persisted for traceability

---

## ⚙️ Enums (Application Layer)

- TaskCategory
- TaskDifficulty
- TaskType
- AvailabilityType
- PeriodType
- AlertType
- AlertSeverity
- TimelineEventType

---

## 📌 Constraints

- user_profiles.user_id UNIQUE
- daily_progress UNIQUE (user_id, progress_date)
- task_completions indexed by (user_id, completion_date)
- rewards.cost_core_points >= 0
- user_profiles.total_xp >= 0
- user_profiles.core_points >= 0

---

## 🚀 Future Extensions

- streaks
- debuffs
- stats tracking
- social features

---

This document represents the MVP database design and will evolve with the system.

---
## 🗄️ Database Schema 2
```mermaid
erDiagram
  USER ||--|| USER_PROFILE : has
  USER_PROFILE ||--o{ TASK : owns
  USER_PROFILE ||--o{ TASK_COMPLETION : logs
  USER_PROFILE ||--o{ REWARD : owns
  USER_PROFILE ||--o{ REWARD_REDEMPTION : redeems
  USER_PROFILE ||--o{ DAILY_SUMMARY : has
  USER_PROFILE ||--o{ ACTIVITY_EVENT : generates
  USER_PROFILE ||--o{ USER_STAT : tracks
  TASK ||--o{ TASK_COMPLETION : completed_via
  TASK }o--o| TASK_CATEGORY : categorized_in
  REWARD ||--o{ REWARD_REDEMPTION : redeemed_via
  REWARD_TEMPLATE ||--o{ REWARD : based_on

  USER {
    uuid id PK
    string username
    string email
    string password_hash
    timestamp created_at
  }

  USER_PROFILE {
    uuid id PK
    uuid user_id FK
    int level
    long xp_total
    long core_points_balance
    int current_streak
    int max_streak
    date last_active_date
    int daily_minimum
    boolean debuff_active
    timestamp debuff_until
    timestamp created_at
  }

  TASK {
    uuid id PK
    uuid user_profile_id FK
    uuid category_id FK
    string name
    string description
    enum type
    enum difficulty
    int base_xp
    boolean is_mandatory
    boolean counts_for_streak
    boolean is_repeatable
    int max_daily_completions
    boolean diminishing_returns
    time expiry_time
    boolean is_active
    timestamp created_at
  }

  TASK_CATEGORY {
    uuid id PK
    string name
    string icon
    boolean is_system
  }

  TASK_COMPLETION {
    uuid id PK
    uuid task_id FK
    uuid user_profile_id FK
    timestamp completed_at
    int completion_number
    int xp_earned
    int core_points_earned
    boolean counted_for_daily
    boolean counted_for_streak
    boolean diminishing_applied
  }

  DAILY_SUMMARY {
    uuid id PK
    uuid user_profile_id FK
    date day
    boolean day_completed
    boolean streak_maintained
    int tasks_completed
    int mandatory_completed
    int mandatory_required
    int xp_earned
    int core_points_earned
    int streak_before
    int streak_after
    boolean debuff_applied
    timestamp evaluated_at
  }

  REWARD {
    uuid id PK
    uuid user_profile_id FK
    uuid template_id FK
    string name
    string description
    int cost_core_points
    int min_level_required
    enum availability_type
    int cooldown_hours
    int period_limit
    timestamp last_redeemed_at
    int period_redeemed_count
    boolean is_active
  }

  REWARD_TEMPLATE {
    uuid id PK
    string name
    string description
    string icon
    int default_cost
    enum availability_type
    int default_cooldown_hours
  }

  REWARD_REDEMPTION {
    uuid id PK
    uuid reward_id FK
    uuid user_profile_id FK
    int core_points_spent
    timestamp redeemed_at
  }

  ACTIVITY_EVENT {
    uuid id PK
    uuid user_profile_id FK
    enum event_type
    string description
    jsonb metadata
    timestamp occurred_at
  }

  USER_STAT {
    uuid id PK
    uuid user_profile_id FK
    enum stat_type
    int value
    timestamp updated_at
  }
```