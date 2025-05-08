-- Create account types table
CREATE TABLE IF NOT EXISTS account_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    account_type_id BIGINT NOT NULL,
    parent_id BIGINT,
    active BOOLEAN DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (account_type_id) REFERENCES account_types(id),
    FOREIGN KEY (parent_id) REFERENCES accounts(id),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Create fiscal years table
CREATE TABLE IF NOT EXISTS fiscal_years (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Create journal entries table
CREATE TABLE IF NOT EXISTS journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,
    entry_date DATE NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    fiscal_year_id BIGINT,
    created_by BIGINT,
    approved_by BIGINT,
    company_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_years(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Create journal entry lines table
CREATE TABLE IF NOT EXISTS journal_entry_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    description VARCHAR(255),
    debit_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    credit_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    line_number INT,
    FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id),
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);