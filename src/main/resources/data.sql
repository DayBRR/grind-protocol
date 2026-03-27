MERGE INTO users (username, password, role, enabled)
    KEY (username)
    VALUES (
    'admin',
    '$2a$10$WYP5.LREBkTgamnp7/xTTeLrzaRBcdNHbjs4VJ0xk/4JB9eZrPIzG',
    'ADMIN',
    TRUE
    );

MERGE INTO users (username, password, role, enabled)
    KEY (username)
    VALUES (
    'user',
    '$2a$10$oORx.RofsB4UbrI1hjsb6uZWs42SQoRgFxQihd4DXcDeEecmTc.IK',
    'USER',
    TRUE
    );