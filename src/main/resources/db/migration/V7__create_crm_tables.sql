-- V7__create_crm_tables.sql
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    tax_id VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    payment_terms VARCHAR(255),
    credit_limit DECIMAL(19,4) DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Crear índice único por separado (sintaxis compatible con H2)
CREATE UNIQUE INDEX uk_customer_tax_company ON customers(tax_id, company_id);

CREATE TABLE suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    tax_id VARCHAR(20),
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    payment_terms VARCHAR(255),
    credit_limit DECIMAL(19,4) DEFAULT 0,
    bank_account VARCHAR(100),
    supplier_category VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Crear índice único por separado
CREATE UNIQUE INDEX uk_supplier_tax_company ON suppliers(tax_id, company_id);

CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    position VARCHAR(100),
    contact_type VARCHAR(20) NOT NULL,
    customer_id BIGINT,
    supplier_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,

    CONSTRAINT chk_contact_relation CHECK (
        (customer_id IS NOT NULL AND supplier_id IS NULL) OR
        (supplier_id IS NOT NULL AND customer_id IS NULL)
    ),
    CONSTRAINT chk_contact_type CHECK (contact_type IN ('COMMERCIAL', 'TECHNICAL', 'ADMIN'))
);

-- Agregar permisos CRM
INSERT INTO permissions (name, description, module) VALUES
('CRM_READ', 'View CRM data', 'crm'),
('CRM_CREATE', 'Create CRM entries', 'crm'),
('CRM_UPDATE', 'Update CRM data', 'crm'),
('CRM_DELETE', 'Delete CRM entries', 'crm');

-- Asignar permisos a roles existentes
INSERT INTO roles_permissions (role_id, permission_id)
SELECT 1, id FROM permissions WHERE module = 'crm';

INSERT INTO roles_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name IN ('CRM_READ', 'CRM_CREATE', 'CRM_UPDATE');