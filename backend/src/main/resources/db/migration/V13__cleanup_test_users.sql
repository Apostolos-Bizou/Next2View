-- V13: Reset test user passwords and delete test users
UPDATE users SET password_hash = '$2b$12$zbrN01M6cjDCGHlDCnRUuOB5z5aJGV.yPCbknli0Y/Ezh8yHb8YgC' WHERE email IN ('test@next2me.com', 'test2@next2me.com');
DELETE FROM user_permissions WHERE user_id IN ('47cc7587-3e2f-49ae-b929-cff83809e75d', '1d61af13-f4c9-4b89-921c-e62aa8b7156d');
DELETE FROM users WHERE email IN ('test@next2me.com', 'test2@next2me.com');