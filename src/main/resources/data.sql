MERGE INTO users (
    username,
    email,
    password,
    role,
    enabled,
    email_verified,
    email_verified_at,
    last_login_at,
    created_at,
    updated_at,
    created_by,
    updated_by
    )
    KEY (username)
    VALUES (
    'admin',
    'admin@test.com',
    '$2a$10$WYP5.LREBkTgamnp7/xTTeLrzaRBcdNHbjs4VJ0xk/4JB9eZrPIzG',
    'ADMIN',
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    1,
    1
    );

MERGE INTO users (
    username,
    email,
    password,
    role,
    enabled,
    email_verified,
    email_verified_at,
    last_login_at,
    created_at,
    updated_at,
    created_by,
    updated_by
    )
    KEY (username)
    VALUES (
    'user',
    'user@test.com',
    '$2a$10$oORx.RofsB4UbrI1hjsb6uZWs42SQoRgFxQihd4DXcDeEecmTc.IK',
    'USER',
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    2,
    2
    );