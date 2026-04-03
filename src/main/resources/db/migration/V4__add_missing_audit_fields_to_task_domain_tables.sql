-- V4__add_missing_audit_fields_to_task_domain_tables.sql

-- =========================================================
-- USER_PROFILES
-- =========================================================
ALTER TABLE user_profiles
    ADD COLUMN created_by BIGINT NULL;

ALTER TABLE user_profiles
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE user_profiles
    ADD CONSTRAINT fk_user_profiles_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE user_profiles
    ADD CONSTRAINT fk_user_profiles_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_user_profiles_created_by ON user_profiles (created_by);
CREATE INDEX idx_user_profiles_updated_by ON user_profiles (updated_by);

-- =========================================================
-- TASK_TEMPLATES
-- =========================================================
ALTER TABLE task_templates
    ADD COLUMN created_by BIGINT NULL;

ALTER TABLE task_templates
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE task_templates
    ADD CONSTRAINT fk_task_templates_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE task_templates
    ADD CONSTRAINT fk_task_templates_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_task_templates_created_by ON task_templates (created_by);
CREATE INDEX idx_task_templates_updated_by ON task_templates (updated_by);

-- =========================================================
-- TASKS
-- =========================================================
ALTER TABLE tasks
    ADD COLUMN created_by BIGINT NULL;

ALTER TABLE tasks
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_tasks_created_by ON tasks (created_by);
CREATE INDEX idx_tasks_updated_by ON tasks (updated_by);

-- =========================================================
-- TASK_COMPLETIONS
-- =========================================================
ALTER TABLE task_completions
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE task_completions
    ADD COLUMN created_by BIGINT NULL;

ALTER TABLE task_completions
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE task_completions
    ADD CONSTRAINT fk_task_completions_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE task_completions
    ADD CONSTRAINT fk_task_completions_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_task_completions_created_by ON task_completions (created_by);
CREATE INDEX idx_task_completions_updated_by ON task_completions (updated_by);

-- =========================================================
-- DAILY_PROGRESS
-- =========================================================
ALTER TABLE daily_progress
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE daily_progress
    ADD COLUMN created_by BIGINT NULL;

ALTER TABLE daily_progress
    ADD COLUMN updated_by BIGINT NULL;

ALTER TABLE daily_progress
    ADD CONSTRAINT fk_daily_progress_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE daily_progress
    ADD CONSTRAINT fk_daily_progress_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_daily_progress_created_by ON daily_progress (created_by);
CREATE INDEX idx_daily_progress_updated_by ON daily_progress (updated_by);