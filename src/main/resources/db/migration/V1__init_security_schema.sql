-- Companies Table
CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    tax_id VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(100),
    website VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Permissions Table
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    module VARCHAR(50)
);

-- Roles Table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Role-Permission Join Table
CREATE TABLE roles_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    company_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- User-Role Join Table
CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Insert initial company
INSERT INTO companies (name, tax_id, address, city, state, postal_code)
VALUES ('Default Company', 'A12345678', 'Main Street 123', 'Default City', 'Default State', '12345');

-- Insert basic permissions
INSERT INTO permissions (name, description, module) VALUES
('USER_READ', 'View users', 'core'),
('USER_CREATE', 'Create users', 'core'),
('USER_UPDATE', 'Update users', 'core'),
('USER_DELETE', 'Delete users', 'core'),
('ROLE_READ', 'View roles', 'core'),
('ROLE_CREATE', 'Create roles', 'core'),
('ROLE_UPDATE', 'Update roles', 'core'),
('ROLE_DELETE', 'Delete roles', 'core');

-- Insert basic roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrator with full access'),
('MANAGER', 'Management access'),
('USER', 'Standard user');

-- Assign permissions to roles
-- Admin gets all permissions
INSERT INTO roles_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- Manager gets view permissions and some edit permissions
INSERT INTO roles_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name LIKE '%_READ' OR name LIKE '%_UPDATE';

-- User gets only view permissions
INSERT INTO roles_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE name LIKE '%_READ';

-- Create admin user (password: admin)
INSERT INTO users (username, password, first_name, last_name, email, company_id)
VALUES ('admin', '$2a$10$ZXamGOFJyQNoVGFJMxlv5OGaL5m7sjwJLw8E0aQCRBNin4T3j.Rei', 'Admin', 'User', 'admin@example.com', 1);

-- Assign admin role to admin user
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);