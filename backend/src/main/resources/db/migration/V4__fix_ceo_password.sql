-- V4: Fix CEO password hash
-- Password: Next2View@2026!
UPDATE users
SET password_hash = '$2a$12$RfZBtXfD66cR5Jf0sZJsE.lib1qd98LGsNZ3PgpfYQVA62DLxjyLq'
WHERE email = 'apostolos@next2me.com';