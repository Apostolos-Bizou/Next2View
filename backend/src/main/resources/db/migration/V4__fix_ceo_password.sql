-- V4: Fix CEO password hash
-- Password: Next2View@2026! (BCrypt cost 12, verified)
UPDATE users
SET password_hash = '$2a$12$tQAHsGBCTYOFOnuOLCMOxuNqAmnKP9GSmkJAJ5UMrI2wVuSBCz1Sq'
WHERE email = 'apostolos@next2me.com';
