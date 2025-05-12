-- Create product categories table
CREATE TABLE product_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Create products table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cost_price DECIMAL(19,4) NOT NULL DEFAULT 0,
    sale_price DECIMAL(19,4) NOT NULL DEFAULT 0,
    stock_quantity INT NOT NULL DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    category_id BIGINT,
    company_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES product_categories(id),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Create inventory transactions table
CREATE TABLE inventory_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    transaction_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    journal_entry_id BIGINT,
    company_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id),
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Create inventory transaction lines table
CREATE TABLE inventory_transaction_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    line_number INT,
    description VARCHAR(255),  -- Added description field
    FOREIGN KEY (transaction_id) REFERENCES inventory_transactions(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Add inventory permissions
INSERT INTO permissions (name, description, module) VALUES
('INVENTORY_READ', 'View inventory data', 'inventory'),
('INVENTORY_CREATE', 'Create inventory entries', 'inventory'),
('INVENTORY_UPDATE', 'Update inventory data', 'inventory'),
('INVENTORY_DELETE', 'Delete inventory entries', 'inventory');

-- Assign inventory permissions to Admin role
INSERT INTO roles_permissions (role_id, permission_id)
SELECT 1, id FROM permissions WHERE module = 'inventory';

-- Assign some inventory permissions to Manager role (read and create only)
INSERT INTO roles_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name IN ('INVENTORY_READ', 'INVENTORY_CREATE');