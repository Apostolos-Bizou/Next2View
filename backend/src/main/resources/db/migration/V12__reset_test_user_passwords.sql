-- V12: Reset test user passwords for UAT
-- Password: Test@2026!
UPDATE users SET password_hash = '$2b$12$zbrN01M6cjDCGHlDCnRUuOB5z5aJGV.yPCbknli0Y/Ezh8yHb8YgC' WHERE email IN ('test@next2me.com', 'test2@next2me.com');