-- V4__insert_sample_data.sql
-- Sample data for testing all features of the Mondo ERP system
-- All data is associated with the Default Company (id = 1)

-- Insert account types (for Chart of Accounts)
INSERT INTO account_types (code, name, description, category, created_at) VALUES
('1000', 'Current Assets', 'Short-term assets that can be converted to cash within one year', 'ASSET', NOW()),
('1500', 'Fixed Assets', 'Long-term tangible assets', 'ASSET', NOW()),
('2000', 'Current Liabilities', 'Short-term debts payable within one year', 'LIABILITY', NOW()),
('2500', 'Long-term Liabilities', 'Obligations not due within a year', 'LIABILITY', NOW()),
('3000', 'Equity', 'Ownership interest in the company', 'EQUITY', NOW()),
('4000', 'Revenue', 'Income from normal business operations', 'REVENUE', NOW()),
('5000', 'Cost of Goods Sold', 'Direct costs of producing goods sold', 'EXPENSE', NOW()),
('6000', 'Expenses', 'Costs incurred in running the business', 'EXPENSE', NOW());

-- Insert accounts for Default Company
INSERT INTO accounts (code, name, description, account_type_id, active, company_id, created_at) VALUES
-- Asset accounts
('1010', 'Cash', 'Cash on hand and in bank', 1, true, 1, NOW()),
('1020', 'Accounts Receivable', 'Amounts owed by customers', 1, true, 1, NOW()),
('1030', 'Inventory', 'Goods available for sale', 1, true, 1, NOW()),
('1040', 'Supplies', 'Office and other supplies', 1, true, 1, NOW()),
('1050', 'Prepaid Expenses', 'Expenses paid in advance', 1, true, 1, NOW()),
('1510', 'Land', 'Cost of land owned', 2, true, 1, NOW()),
('1520', 'Buildings', 'Cost of buildings owned', 2, true, 1, NOW()),
('1530', 'Equipment', 'Cost of equipment owned', 2, true, 1, NOW()),
('1540', 'Vehicles', 'Cost of vehicles owned', 2, true, 1, NOW()),
('1550', 'Furniture and Fixtures', 'Office furniture and fixtures', 2, true, 1, NOW()),
('1590', 'Accumulated Depreciation', 'Accumulated depreciation of fixed assets', 2, true, 1, NOW()),

-- Liability accounts
('2010', 'Accounts Payable', 'Amounts owed to suppliers', 3, true, 1, NOW()),
('2020', 'Wages Payable', 'Amounts owed to employees', 3, true, 1, NOW()),
('2030', 'Interest Payable', 'Interest owed but not yet paid', 3, true, 1, NOW()),
('2040', 'Taxes Payable', 'Taxes owed but not yet paid', 3, true, 1, NOW()),
('2050', 'Unearned Revenue', 'Payments received for goods/services not yet delivered', 3, true, 1, NOW()),
('2510', 'Long-term Loans', 'Loans due beyond one year', 4, true, 1, NOW()),
('2520', 'Mortgage Payable', 'Mortgage on buildings', 4, true, 1, NOW()),
('2530', 'Bonds Payable', 'Bonds issued by the company', 4, true, 1, NOW()),

-- Equity accounts
('3010', 'Common Stock', 'Shares issued to investors', 5, true, 1, NOW()),
('3020', 'Retained Earnings', 'Accumulated profits retained in the business', 5, true, 1, NOW()),
('3030', 'Dividends', 'Distributions to shareholders', 5, true, 1, NOW()),

-- Revenue accounts
('4010', 'Sales Revenue', 'Revenue from sales of goods', 6, true, 1, NOW()),
('4020', 'Service Revenue', 'Revenue from providing services', 6, true, 1, NOW()),
('4030', 'Interest Income', 'Interest earned on investments', 6, true, 1, NOW()),
('4040', 'Rental Income', 'Income from renting property', 6, true, 1, NOW()),

-- Cost of Goods Sold accounts
('5010', 'Cost of Goods Sold', 'Cost of inventory sold', 7, true, 1, NOW()),
('5020', 'Freight-in', 'Shipping costs for incoming inventory', 7, true, 1, NOW()),
('5030', 'Purchase Returns', 'Returns of purchased inventory', 7, true, 1, NOW()),
('5040', 'Purchase Discounts', 'Discounts on purchased inventory', 7, true, 1, NOW()),

-- Expense accounts
('6010', 'Rent Expense', 'Cost of renting facilities', 8, true, 1, NOW()),
('6020', 'Utilities Expense', 'Costs of electricity, water, etc.', 8, true, 1, NOW()),
('6030', 'Salaries Expense', 'Cost of employee salaries', 8, true, 1, NOW()),
('6040', 'Advertising Expense', 'Cost of advertising', 8, true, 1, NOW()),
('6050', 'Insurance Expense', 'Cost of insurance', 8, true, 1, NOW()),
('6060', 'Depreciation Expense', 'Depreciation of fixed assets', 8, true, 1, NOW()),
('6070', 'Office Supplies Expense', 'Cost of office supplies', 8, true, 1, NOW()),
('6080', 'Travel Expense', 'Cost of business travel', 8, true, 1, NOW());

-- Insert a fiscal year for Default Company
INSERT INTO fiscal_years (name, start_date, end_date, status, company_id, created_at) VALUES
('FY2024', '2024-01-01', '2024-12-31', 'OPEN', 1, NOW()),
('FY2025', '2025-01-01', '2025-12-31', 'OPEN', 1, NOW());

-- Insert journal entries for Default Company
INSERT INTO journal_entries (reference, entry_date, description, status, fiscal_year_id, created_by, company_id, created_at) VALUES
('JE-2024-001', '2024-01-15', 'Initial capital investment', 'APPROVED', 1, 1, 1, NOW()),
('JE-2024-002', '2024-02-01', 'Purchase of office equipment', 'APPROVED', 1, 1, 1, NOW()),
('JE-2024-003', '2024-02-15', 'Payment of rent for Q1', 'APPROVED', 1, 1, 1, NOW()),
('JE-2024-004', '2024-03-10', 'Sale of services to Client A', 'APPROVED', 1, 1, 1, NOW()),
('JE-2024-005', '2024-04-05', 'Payment of utilities', 'APPROVED', 1, 1, 1, NOW());

-- Insert journal entry lines
-- Initial capital investment
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number) VALUES
(1, 1, 'Initial cash investment', 100000.00, 0.00, 1),
(1, 21, 'Initial capital stock issued', 0.00, 100000.00, 2);

-- Purchase of office equipment
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number) VALUES
(2, 8, 'Office equipment purchase', 5000.00, 0.00, 1),
(2, 1, 'Payment for office equipment', 0.00, 5000.00, 2);

-- Payment of rent for Q1
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number) VALUES
(3, 30, 'Q1 rent expense', 3600.00, 0.00, 1),
(3, 1, 'Payment of Q1 rent', 0.00, 3600.00, 2);

-- Sale of services to Client A
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number) VALUES
(4, 2, 'Services rendered to Client A', 2500.00, 0.00, 1),
(4, 25, 'Revenue from services to Client A', 0.00, 2500.00, 2);

-- Payment of utilities
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number) VALUES
(5, 31, 'Utilities for Q1', 850.00, 0.00, 1),
(5, 1, 'Payment of utilities', 0.00, 850.00, 2);

-- Insert product categories for Default Company
INSERT INTO product_categories (name, description, company_id, created_at) VALUES
('Electronics', 'Electronic equipment and accessories', 1, NOW()),
('Office Supplies', 'Items used in office operations', 1, NOW()),
('Furniture', 'Office furniture items', 1, NOW()),
('Software', 'Software licenses and subscriptions', 1, NOW());

-- Insert products for Default Company
INSERT INTO products (code, name, description, cost_price, sale_price, stock_quantity, unit, category_id, company_id, active, created_at) VALUES
-- Electronics
('E001', 'Laptop Computer', 'Business laptop with 16GB RAM, 512GB SSD', 800.00, 1200.00, 15, 'pcs', 1, 1, true, NOW()),
('E002', 'Desktop Computer', 'Business desktop with 16GB RAM, 1TB HDD', 600.00, 900.00, 10, 'pcs', 1, 1, true, NOW()),
('E003', 'Smartphone', 'Business smartphone with 128GB storage', 400.00, 600.00, 20, 'pcs', 1, 1, true, NOW()),
('E004', 'Tablet', '10-inch tablet with 64GB storage', 250.00, 400.00, 12, 'pcs', 1, 1, true, NOW()),
('E005', 'Wireless Mouse', 'Ergonomic wireless mouse', 15.00, 25.00, 30, 'pcs', 1, 1, true, NOW()),
('E006', 'Wireless Keyboard', 'Ergonomic wireless keyboard', 20.00, 35.00, 25, 'pcs', 1, 1, true, NOW()),
('E007', 'Monitor 24"', '24-inch LCD monitor', 120.00, 180.00, 18, 'pcs', 1, 1, true, NOW()),

-- Office Supplies
('S001', 'Copy Paper', 'A4 size, 80gsm, 500 sheets per ream', 3.50, 5.00, 100, 'reams', 2, 1, true, NOW()),
('S002', 'Ballpoint Pens', 'Pack of 12 pens, blue ink', 2.50, 4.00, 50, 'packs', 2, 1, true, NOW()),
('S003', 'Sticky Notes', '3x3 inches, pack of 5 pads, 100 sheets each', 2.00, 3.50, 60, 'packs', 2, 1, true, NOW()),
('S004', 'File Folders', 'Letter size, box of 100', 8.00, 12.00, 40, 'boxes', 2, 1, true, NOW()),
('S005', 'Stapler', 'Desktop stapler with 1000 staples', 5.00, 8.00, 25, 'pcs', 2, 1, true, NOW()),
('S006', 'Printer Toner', 'Black toner cartridge', 45.00, 65.00, 15, 'pcs', 2, 1, true, NOW()),

-- Furniture
('F001', 'Office Chair', 'Ergonomic office chair with adjustable height', 80.00, 120.00, 10, 'pcs', 3, 1, true, NOW()),
('F002', 'Office Desk', 'Standard office desk 150x75cm', 150.00, 220.00, 5, 'pcs', 3, 1, true, NOW()),
('F003', 'Bookshelf', '5-shelf bookcase, 180cm tall', 70.00, 100.00, 8, 'pcs', 3, 1, true, NOW()),
('F004', 'Filing Cabinet', '4-drawer metal filing cabinet', 100.00, 150.00, 6, 'pcs', 3, 1, true, NOW()),
('F005', 'Conference Table', 'Oval conference table, seats 8', 200.00, 300.00, 2, 'pcs', 3, 1, true, NOW()),

-- Software
('SW001', 'Office Suite', 'Productivity software license, 1 year', 100.00, 150.00, 25, 'licenses', 4, 1, true, NOW()),
('SW002', 'Accounting Software', 'Accounting software license, 1 year', 200.00, 300.00, 10, 'licenses', 4, 1, true, NOW()),
('SW003', 'Antivirus Software', 'Antivirus software license, 1 year', 30.00, 50.00, 20, 'licenses', 4, 1, true, NOW()),
('SW004', 'Project Management', 'Project management software, 1 year', 150.00, 220.00, 8, 'licenses', 4, 1, true, NOW());

-- Insert inventory transactions for Default Company
-- Purchase transactions
INSERT INTO inventory_transactions (reference, transaction_date, type, description, company_id, created_by, created_at) VALUES
('PUR-2024-001', '2024-01-20', 'PURCHASE', 'Initial inventory purchase - Electronics', 1, 1, NOW()),
('PUR-2024-002', '2024-01-21', 'PURCHASE', 'Initial inventory purchase - Office Supplies', 1, 1, NOW()),
('PUR-2024-003', '2024-01-22', 'PURCHASE', 'Initial inventory purchase - Furniture', 1, 1, NOW()),
('PUR-2024-004', '2024-01-23', 'PURCHASE', 'Initial inventory purchase - Software', 1, 1, NOW());

-- Sale transactions
INSERT INTO inventory_transactions (reference, transaction_date, type, description, company_id, created_by, created_at) VALUES
('SAL-2024-001', '2024-02-05', 'SALE', 'Sale to Client A - Office setup', 1, 1, NOW()),
('SAL-2024-002', '2024-02-12', 'SALE', 'Sale to Client B - Software licenses', 1, 1, NOW()),
('SAL-2024-003', '2024-03-03', 'SALE', 'Sale to Client C - Office supplies', 1, 1, NOW());

-- Adjustment transaction
INSERT INTO inventory_transactions (reference, transaction_date, type, description, company_id, created_by, created_at) VALUES
('ADJ-2024-001', '2024-03-15', 'ADJUSTMENT', 'Inventory count adjustment - Electronics', 1, 1, NOW());

-- Inventory transaction lines
-- PUR-2024-001 (Electronics)
INSERT INTO inventory_transaction_lines (transaction_id, product_id, quantity, unit_price, line_number) VALUES
(1, 1, 15, 800.00, 1),  -- Laptops
(1, 2, 10, 600.00, 2),  -- Desktops
(1, 3, 20, 400.00, 3),  -- Smartphones
(1, 4, 12, 250.00, 4),  -- Tablets
(1, 5, 30, 15.00, 5),   -- Wireless Mice
(1, 6, 25, 20.00, 6),   -- Wireless Keyboards
(1, 7, 18, 120.00, 7);  -- Monitors

-- PUR-2024-002 (Office Supplies)
INSERT INTO inventory_transaction_lines (transaction_id, product_id, quantity, unit_price, line_number) VALUES
(2, 8, 100, 3.50, 1),   -- Copy Paper
(2, 9, 50, 2.50, 2),    -- Ballpoint Pens
(2, 10, 60, 2.00, 3),   -- Sticky Notes
(2, 11, 40, 8.00, 4),   -- File Folders
(2, 12, 25, 5.00, 5),   -- Stapler
(2, 13, 15, 45.00, 6);  -- Printer Toner

-- PUR-2024-003 (Furniture)
INSERT INTO inventory_transaction_lines (transaction_id, product_id, quantity, unit_price, line_number) VALUES
(3, 14, 10, 80.00, 1),   -- Office Chairs
(3, 15, 5, 150.00, 2),   -- Office Desks
(3, 16, 8, 70.00, 3),    -- Bookshelves
(3, 17, 6, 100.00, 4),   -- Filing Cabinets
(3, 18, 2, 200.00, 5);   -- Conference Tables

-- PUR-2024-004 (Software)
INSERT INTO inventory_transaction_lines (transaction_id, product_id, quantity, unit_price, line_number) VALUES
(4, 19, 25, 100.00, 1),  -- Office Suite
(4, 20, 10, 200.00, 2),  -- Accounting Software
(4, 21, 20, 30.00, 3),   -- Antivirus Software
(4, 22, 8, 150.00, 4);   -- Project Management

-- SAL-2024-001 (Sale to Client A - Office setup)
INSERT INTO inventory_transaction_lines (transaction_id, product_id, quantity, unit_price, line_number) VALUES
(5, 1, 2, 1200.00, 1),    -- Laptops
(5, 7, 2, 180.00, 2),     -- Monitors
(5, 14, 2, 120.00, 3),    -- Office Chairs
(5, 15, 2, 220.00, 4),    -- Office Desks
(5, 19, 2, 150.00, 5);    -- Office Suite

-- SAL-2024-002 (Sale to Client B - Software licenses)
INSERT INTO inventory_transaction_lines (transaction_id, product_id, quantity, unit_price, line_number) VALUES
(6, 19, 5, 150.00, 1),    -- Office Suite
(6, 20, 2, 300.00, 2),    -- Accounting Software
(6, 21, 5, 50.00, 3);     -- Antivirus Software

-- SAL-2024-003 (Sale to Client C - Office supplies)
INSERT INTO inventory_transaction_lines (transaction_id, product_id, quantity, unit_price, line_number) VALUES
(7, 8, 10, 5.00, 1),      -- Copy Paper
(7, 9, 5, 4.00, 2),       -- Ballpoint Pens
(7, 10, 5, 3.50, 3),      -- Sticky Notes
(7, 11, 5, 12.00, 4);     -- File Folders

-- ADJ-2024-001 (Inventory count adjustment - Electronics)
INSERT INTO inventory_transaction_lines (transaction_id, product_id, quantity, unit_price, line_number) VALUES
(8, 5, 28, 15.00, 1),     -- Wireless Mice (loss of 2)
(8, 6, 24, 20.00, 2);     -- Wireless Keyboards (loss of 1)

-- Add a test manager user (password: manager)
INSERT INTO users (username, password, first_name, last_name, email, active, company_id, created_at)
VALUES ('manager', '$2a$10$ZXamGOFJyQNoVGFJMxlv5OGaL5m7sjwJLw8E0aQCRBNin4T3j.Rei', 'Test', 'Manager', 'manager@example.com', true, 1, NOW());

-- Assign manager role to the test manager
INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);

-- Add a test regular user (password: user)
INSERT INTO users (username, password, first_name, last_name, email, active, company_id, created_at)
VALUES ('user', '$2a$10$ZXamGOFJyQNoVGFJMxlv5OGaL5m7sjwJLw8E0aQCRBNin4T3j.Rei', 'Test', 'User', 'user@example.com', true, 1, NOW());

-- Assign user role to the test user
INSERT INTO users_roles (user_id, role_id) VALUES (3, 3);