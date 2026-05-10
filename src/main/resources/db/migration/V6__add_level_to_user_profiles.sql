ALTER TABLE user_profiles
    ADD COLUMN current_level INT NOT NULL DEFAULT 1;

UPDATE user_profiles
SET current_level = CAST((total_xp / 100) + 1 AS INT);

ALTER TABLE user_profiles
    ADD CONSTRAINT chk_user_profiles_current_level CHECK (current_level >= 1);