-- V2__update_users_audit_and_email.sql

-- 1. Nuevos campos de identidad
ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP NULL;

-- 2. Nuevos campos de auditoría
ALTER TABLE users ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN created_by BIGINT NULL;
ALTER TABLE users ADD COLUMN updated_by BIGINT NULL;

-- 3. Relleno inicial para filas existentes
UPDATE users
SET email = username || '@temp.local'
WHERE email IS NULL;

-- 4. Restricciones
ALTER TABLE users ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT fk_users_created_by
        FOREIGN KEY (created_by) REFERENCES users (id);

ALTER TABLE users
    ADD CONSTRAINT fk_users_updated_by
        FOREIGN KEY (updated_by) REFERENCES users (id);

-- 5. Índices útiles
CREATE INDEX idx_users_created_by ON users (created_by);
CREATE INDEX idx_users_updated_by ON users (updated_by);
CREATE INDEX idx_users_email_verified ON users (email_verified);