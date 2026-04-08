-- Next2View V3: Initial Seed Data

INSERT INTO companies (id, name, code, color, description) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Polaris Financial Services', 'PF', '#3b82f6', 'TPA maritime healthcare'),
    ('a1000000-0000-0000-0000-000000000002', 'Crossworld Marine Services', 'CW', '#059669', 'Crew management'),
    ('a1000000-0000-0000-0000-000000000003', 'WiMAS Training Center',      'WM', '#d97706', 'Maritime training'),
    ('a1000000-0000-0000-0000-000000000004', 'Varship Management',         'VS', '#7c3aed', 'Ship management'),
    ('a1000000-0000-0000-0000-000000000005', 'Oceansoft',                  'OS', '#6b7280', 'IT solutions');

INSERT INTO users (id, full_name, email, password_hash, role, department, company_id, mfa_enabled) VALUES
    (
        'b1000000-0000-0000-0000-000000000001',
        'Απόστολος Βίζου',
        'apostolos@next2me.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TiGT.7Rb.bXTwzqVOJyW7qGKCZFi',
        'CEO',
        'management',
        'a1000000-0000-0000-0000-000000000001',
        FALSE
    );

INSERT INTO audit_log (user_email, action, entity_type, new_value) VALUES
    ('system', 'SEED', 'companies', '{"count": 5}'),
    ('system', 'SEED', 'users',     '{"count": 1}');
