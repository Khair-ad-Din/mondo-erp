-- Add Finance permissions
INSERT INTO permissions (name, description, module) VALUES
('FINANCE_READ', 'View financial data', 'finance'),
('FINANCE_CREATE', 'Create financial entries', 'finance'),
('FINANCE_UPDATE', 'Update financial data', 'finance'),
('FINANCE_DELETE', 'Delete financial entries', 'finance'),
('FINANCE_POST', 'Post journal entries', 'finance'),
('FINANCE_REPORT', 'Generate financial reports', 'finance');

-- Assign Finance permissions to Admin role
INSERT INTO roles_permissions (role_id, permission_id)
SELECT 1, id FROM permissions WHERE module = 'finance';

-- Assign some Finance permissions to Manager role (read and create only)
INSERT INTO roles_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name IN ('FINANCE_READ', 'FINANCE_CREATE', 'FINANCE_REPORT');