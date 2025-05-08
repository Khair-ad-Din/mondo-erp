-- Sample Account Types
INSERT INTO account_types (code, name, description, category) VALUES
('1000', 'Current Assets', 'Cash and assets expected to be converted to cash within one year', 'ASSET'),
('1500', 'Fixed Assets', 'Long-term tangible assets', 'ASSET'),
('2000', 'Current Liabilities', 'Debts due within one year', 'LIABILITY'),
('2500', 'Long-term Liabilities', 'Debts due beyond one year', 'LIABILITY'),
('3000', 'Equity', 'Ownership interest in the business', 'EQUITY'),
('4000', 'Revenue', 'Income from business operations', 'REVENUE'),
('5000', 'Cost of Goods Sold', 'Direct costs related to product sales', 'EXPENSE'),
('6000', 'Operating Expenses', 'Day-to-day business expenses', 'EXPENSE');

-- Sample Accounts (assuming company_id=1 exists)
-- Assets
INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('1010', 'Cash', 'Cash on hand and in bank accounts', 1, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('1020', 'Accounts Receivable', 'Amounts due from customers', 1, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('1030', 'Inventory', 'Goods available for sale', 1, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('1510', 'Furniture and Equipment', 'Office furniture and equipment', 2, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('1520', 'Vehicles', 'Company vehicles', 2, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('1530', 'Accumulated Depreciation', 'Accumulated depreciation of fixed assets', 2, NULL, true, 1);

-- Liabilities
INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('2010', 'Accounts Payable', 'Amounts owed to vendors', 3, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('2020', 'Salaries Payable', 'Salaries owed to employees', 3, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('2030', 'Taxes Payable', 'Taxes owed to government', 3, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('2510', 'Long-term Loans', 'Bank loans due after one year', 4, NULL, true, 1);

-- Equity
INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('3010', 'Owner''s Capital', 'Owner''s investment in the business', 5, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('3020', 'Retained Earnings', 'Accumulated profits reinvested in the business', 5, NULL, true, 1);

-- Revenue
INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('4010', 'Sales Revenue', 'Income from sales of products or services', 6, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('4020', 'Service Revenue', 'Income from providing services', 6, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('4030', 'Interest Income', 'Income from interest on investments', 6, NULL, true, 1);

-- Cost of Goods Sold
INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('5010', 'Purchases', 'Cost of goods purchased for resale', 7, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('5020', 'Freight In', 'Shipping costs for incoming goods', 7, NULL, true, 1);

-- Operating Expenses
INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('6010', 'Rent Expense', 'Office or store rental costs', 8, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('6020', 'Utilities Expense', 'Electricity, water, gas, internet', 8, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('6030', 'Salaries Expense', 'Employee salaries', 8, NULL, true, 1);

INSERT INTO accounts (code, name, description, account_type_id, parent_id, active, company_id)
VALUES ('6040', 'Advertising Expense', 'Costs for advertising and marketing', 8, NULL, true, 1);

-- Create a Fiscal Year
INSERT INTO fiscal_years (name, start_date, end_date, status, company_id)
VALUES ('FY2025', '2025-01-01', '2025-12-31', 'OPEN', 1);

-- Sample Journal Entries
-- Initial investment
INSERT INTO journal_entries (reference, entry_date, description, status, fiscal_year_id, created_by, company_id)
VALUES ('JE-2025-001', '2025-01-05', 'Initial investment by owner', 'POSTED', 1, 1, 1);

-- Insert the journal entry lines for the initial investment
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number)
VALUES
(1, 1, 'Initial investment - Cash', 50000.00, 0.00, 1),
(1, 11, 'Initial investment - Owner''s capital', 0.00, 50000.00, 2);

-- Purchase of equipment
INSERT INTO journal_entries (reference, entry_date, description, status, fiscal_year_id, created_by, company_id)
VALUES ('JE-2025-002', '2025-01-10', 'Purchase of office equipment', 'POSTED', 1, 1, 1);

-- Insert the journal entry lines for the equipment purchase
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number)
VALUES
(2, 4, 'Office furniture and computers', 12000.00, 0.00, 1),
(2, 1, 'Payment for equipment', 0.00, 12000.00, 2);

-- Sales transaction
INSERT INTO journal_entries (reference, entry_date, description, status, fiscal_year_id, created_by, company_id)
VALUES ('JE-2025-003', '2025-01-15', 'Sales revenue for first half of January', 'POSTED', 1, 1, 1);

-- Insert the journal entry lines for the sales transaction
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number)
VALUES
(3, 1, 'Cash from sales', 8500.00, 0.00, 1),
(3, 13, 'Sales revenue', 0.00, 8500.00, 2);

-- Rent payment
INSERT INTO journal_entries (reference, entry_date, description, status, fiscal_year_id, created_by, company_id)
VALUES ('JE-2025-004', '2025-01-31', 'Monthly rent payment', 'POSTED', 1, 1, 1);

-- Insert the journal entry lines for the rent payment
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number)
VALUES
(4, 18, 'Office rent for January', 2000.00, 0.00, 1),
(4, 1, 'Payment of rent', 0.00, 2000.00, 2);

-- Utility payment
INSERT INTO journal_entries (reference, entry_date, description, status, fiscal_year_id, created_by, company_id)
VALUES ('JE-2025-005', '2025-01-31', 'Monthly utilities payment', 'POSTED', 1, 1, 1);

-- Insert the journal entry lines for the utility payment
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number)
VALUES
(5, 19, 'Utilities for January', 850.00, 0.00, 1),
(5, 1, 'Payment of utilities', 0.00, 850.00, 2);

-- Draft journal entry (not posted)
INSERT INTO journal_entries (reference, entry_date, description, status, fiscal_year_id, created_by, company_id)
VALUES ('JE-2025-006', '2025-02-01', 'Purchase of inventory', 'DRAFT', 1, 1, 1);

-- Insert the journal entry lines for the inventory purchase
INSERT INTO journal_entry_lines (journal_entry_id, account_id, description, debit_amount, credit_amount, line_number)
VALUES
(6, 3, 'Inventory purchase', 5000.00, 0.00, 1),
(6, 7, 'Accounts payable to supplier', 0.00, 5000.00, 2);