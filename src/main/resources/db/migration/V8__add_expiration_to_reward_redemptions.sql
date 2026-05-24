ALTER TABLE reward_redemptions
    ADD COLUMN expires_at TIMESTAMP NULL;

CREATE INDEX idx_reward_redemptions_expires_at
    ON reward_redemptions (expires_at);