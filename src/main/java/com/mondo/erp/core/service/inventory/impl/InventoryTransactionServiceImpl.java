package com.mondo.erp.core.service.inventory.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.finance.Account;
import com.mondo.erp.core.model.finance.FiscalYear;
import com.mondo.erp.core.model.finance.JournalEntry;
import com.mondo.erp.core.model.finance.JournalEntryLine;
import com.mondo.erp.core.model.inventory.InventoryTransaction;
import com.mondo.erp.core.model.inventory.InventoryTransactionLine;
import com.mondo.erp.core.model.inventory.Product;
import com.mondo.erp.core.repository.inventory.InventoryTransactionRepository;
import com.mondo.erp.core.service.finance.AccountService;
import com.mondo.erp.core.service.finance.FiscalYearService;
import com.mondo.erp.core.service.finance.JournalEntryService;
import com.mondo.erp.core.service.inventory.InventoryTransactionService;
import com.mondo.erp.core.service.inventory.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final ProductService productService;
    private final JournalEntryService journalEntryService;
    private final AccountService accountService;
    private final FiscalYearService fiscalYearService;

    // Account codes for inventory accounting
    private static final String INVENTORY_ACCOUNT_CODE = "1030"; // Asset
    private static final String ACCOUNTS_PAYABLE_ACCOUNT_CODE = "2010"; // Liability
    private static final String CASH_ACCOUNT_CODE = "1010"; // Asset
    private static final String ACCOUNTS_RECEIVABLE_ACCOUNT_CODE = "1020"; // Asset
    private static final String SALES_REVENUE_ACCOUNT_CODE = "4010"; // Revenue
    private static final String COST_OF_GOODS_SOLD_ACCOUNT_CODE = "5010"; // Expense

    @Autowired
    public InventoryTransactionServiceImpl(
            InventoryTransactionRepository transactionRepository,
            ProductService productService,
            JournalEntryService journalEntryService,
            AccountService accountService,
            FiscalYearService fiscalYearService) {
        this.transactionRepository = transactionRepository;
        this.productService = productService;
        this.journalEntryService = journalEntryService;
        this.accountService = accountService;
        this.fiscalYearService = fiscalYearService;
    }

    @Override
    public List<InventoryTransaction> findByCompany(Company company) {
        return transactionRepository.findByCompany(company);
    }

    @Override
    public List<InventoryTransaction> findByCompanyAndType(Company company, InventoryTransaction.TransactionType type) {
        return transactionRepository.findByCompanyAndType(company, type);
    }

    @Override
    public List<InventoryTransaction> findByCompanyAndDateRange(Company company, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByCompanyAndTransactionDateBetween(company, startDate, endDate);
    }

    @Override
    public Optional<InventoryTransaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Optional<InventoryTransaction> findByReferenceAndCompany(String reference, Company company) {
        return transactionRepository.findByReferenceAndCompany(reference, company);
    }

    @Override
    @Transactional
    public InventoryTransaction save(InventoryTransaction transaction) {
        // This is a basic save without accounting integration
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public InventoryTransaction createPurchase(InventoryTransaction purchase, String accountingMethod, User user) {
        if (purchase.getType() != InventoryTransaction.TransactionType.PURCHASE) {
            throw new IllegalArgumentException("Transaction must be of type PURCHASE");
        }

        // Set line numbers and calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < purchase.getTransactionLines().size(); i++) {
            InventoryTransactionLine line = purchase.getTransactionLines().get(i);
            line.setTransaction(purchase);
            line.setLineNumber(i + 1);

            // Calculate line total
            BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            // Update product stock
            Product product = line.getProduct();
            product.setStockQuantity(product.getStockQuantity() + line.getQuantity());
            productService.save(product);
        }

        // Set transaction fields
        purchase.setCreatedBy(user);

        // Create related journal entry
        JournalEntry journalEntry = createPurchaseJournalEntry(purchase, accountingMethod, user, totalAmount);
        purchase.setJournalEntry(journalEntry);

        // Save and return the transaction
        return transactionRepository.save(purchase);
    }

    @Override
    @Transactional
    public InventoryTransaction createSale(InventoryTransaction sale, String accountingMethod, User user) {
        if (sale.getType() != InventoryTransaction.TransactionType.SALE) {
            throw new IllegalArgumentException("Transaction must be of type SALE");
        }

        // Set line numbers and calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalCostAmount = BigDecimal.ZERO;

        for (int i = 0; i < sale.getTransactionLines().size(); i++) {
            InventoryTransactionLine line = sale.getTransactionLines().get(i);
            line.setTransaction(sale);
            line.setLineNumber(i + 1);

            // Calculate line total (revenue)
            BigDecimal lineTotal = line.getUnitPrice().multiply(new BigDecimal(line.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            // Calculate cost amount for COGS
            Product product = line.getProduct();
            BigDecimal costAmount = product.getCostPrice().multiply(new BigDecimal(line.getQuantity()));
            totalCostAmount = totalCostAmount.add(costAmount);

            // Check if enough stock
            if (product.getStockQuantity() < line.getQuantity()) {
                throw new IllegalStateException("Not enough stock for product: " + product.getName());
            }

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - line.getQuantity());
            productService.save(product);
        }

        // Set transaction fields
        sale.setCreatedBy(user);

        // Create related journal entry
        JournalEntry journalEntry = createSaleJournalEntry(sale, accountingMethod, user, totalAmount, totalCostAmount);
        sale.setJournalEntry(journalEntry);

        // Save and return the transaction
        return transactionRepository.save(sale);
    }

    @Override
    @Transactional
    public InventoryTransaction createAdjustment(InventoryTransaction adjustment, User user) {
        if (adjustment.getType() != InventoryTransaction.TransactionType.ADJUSTMENT) {
            throw new IllegalArgumentException("Transaction must be of type ADJUSTMENT");
        }

        // Set line numbers and process adjustments
        BigDecimal totalAdjustmentValue = BigDecimal.ZERO;

        for (int i = 0; i < adjustment.getTransactionLines().size(); i++) {
            InventoryTransactionLine line = adjustment.getTransactionLines().get(i);
            line.setTransaction(adjustment);
            line.setLineNumber(i + 1);

            Product product = line.getProduct();
            int oldQuantity = product.getStockQuantity();
            int adjustmentQuantity = line.getQuantity();

            // For inventory adjustments, quantity represents the final quantity
            // Positive adjustment value if increasing stock, negative if decreasing
            BigDecimal adjustmentValue = product.getCostPrice().multiply(
                    new BigDecimal(adjustmentQuantity - oldQuantity));

            totalAdjustmentValue = totalAdjustmentValue.add(adjustmentValue);

            // Update product stock to the new quantity
            product.setStockQuantity(adjustmentQuantity);
            productService.save(product);
        }

        // Set transaction fields
        adjustment.setCreatedBy(user);

        // Create related journal entry if adjustment has value
        if (totalAdjustmentValue.compareTo(BigDecimal.ZERO) != 0) {
            JournalEntry journalEntry = createAdjustmentJournalEntry(adjustment, user, totalAdjustmentValue);
            adjustment.setJournalEntry(journalEntry);
        }

        // Save and return the transaction
        return transactionRepository.save(adjustment);
    }

    @Override
    public String generateNextReference(Company company, InventoryTransaction.TransactionType type) {
        // Find the highest reference number for this type
        String prefix = getTransactionTypePrefix(type) + "-" + LocalDate.now().getYear() + "-";

        List<InventoryTransaction> transactions = transactionRepository.findByCompanyAndType(company, type);

        int maxNumber = 0;
        for (InventoryTransaction transaction : transactions) {
            String reference = transaction.getReference();
            if (reference.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(reference.substring(prefix.length()));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore if format is different
                }
            }
        }

        // Format with leading zeros
        return prefix + String.format("%03d", maxNumber + 1);
    }

    @Override
    public boolean isReferenceUnique(String reference, Company company, Long excludeId) {
        Optional<InventoryTransaction> existingTransaction = transactionRepository.findByReferenceAndCompany(reference, company);
        return !existingTransaction.isPresent() || existingTransaction.get().getId().equals(excludeId);
    }

    // Private helper methods for accounting integration

    private String getTransactionTypePrefix(InventoryTransaction.TransactionType type) {
        switch (type) {
            case PURCHASE:
                return "PUR";
            case SALE:
                return "SAL";
            case ADJUSTMENT:
                return "ADJ";
            default:
                return "INV";
        }
    }

    private JournalEntry createPurchaseJournalEntry(
            InventoryTransaction purchase,
            String accountingMethod,
            User user,
            BigDecimal totalAmount) {

        Company company = purchase.getCompany();

        // Find appropriate accounts
        Account inventoryAccount = getAccountByCode(INVENTORY_ACCOUNT_CODE, company);
        Account contraAccount;

        if ("AP".equals(accountingMethod)) {
            contraAccount = getAccountByCode(ACCOUNTS_PAYABLE_ACCOUNT_CODE, company);
        } else {
            contraAccount = getAccountByCode(CASH_ACCOUNT_CODE, company);
        }

        // Create journal entry
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setReference("INV-" + purchase.getReference());
        journalEntry.setEntryDate(purchase.getTransactionDate());
        journalEntry.setDescription("Inventory purchase: " + purchase.getReference());
        journalEntry.setStatus(JournalEntry.JournalEntryStatus.POSTED);
        journalEntry.setCompany(company);
        journalEntry.setCreatedBy(user);

        // Find fiscal year
        Optional<FiscalYear> fiscalYear = fiscalYearService.findByDate(purchase.getTransactionDate(), company);
        fiscalYear.ifPresent(journalEntry::setFiscalYear);

        // Create journal entry lines
        List<JournalEntryLine> lines = new ArrayList<>();

        // Debit Inventory (Asset increases)
        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setJournalEntry(journalEntry);
        debitLine.setAccount(inventoryAccount);
        debitLine.setDescription("Inventory purchase");
        debitLine.setDebitAmount(totalAmount);
        debitLine.setCreditAmount(BigDecimal.ZERO);
        debitLine.setLineNumber(1);
        lines.add(debitLine);

        // Credit Accounts Payable/Cash (Liability increases or Asset decreases)
        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setJournalEntry(journalEntry);
        creditLine.setAccount(contraAccount);
        creditLine.setDescription("AP".equals(accountingMethod) ? "Accounts Payable for purchase" : "Cash payment for purchase");
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(totalAmount);
        creditLine.setLineNumber(2);
        lines.add(creditLine);

        journalEntry.setEntryLines(lines);

        // Save and return
        return journalEntryService.save(journalEntry);
    }

    private JournalEntry createSaleJournalEntry(
            InventoryTransaction sale,
            String accountingMethod,
            User user,
            BigDecimal totalAmount,
            BigDecimal totalCostAmount) {

        Company company = sale.getCompany();

        // Find appropriate accounts
        Account inventoryAccount = getAccountByCode(INVENTORY_ACCOUNT_CODE, company);
        Account cogsAccount = getAccountByCode(COST_OF_GOODS_SOLD_ACCOUNT_CODE, company);
        Account revenueAccount = getAccountByCode(SALES_REVENUE_ACCOUNT_CODE, company);
        Account receivableAccount;

        if ("AR".equals(accountingMethod)) {
            receivableAccount = getAccountByCode(ACCOUNTS_RECEIVABLE_ACCOUNT_CODE, company);
        } else {
            receivableAccount = getAccountByCode(CASH_ACCOUNT_CODE, company);
        }

        // Create journal entry
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setReference("INV-" + sale.getReference());
        journalEntry.setEntryDate(sale.getTransactionDate());
        journalEntry.setDescription("Inventory sale: " + sale.getReference());
        journalEntry.setStatus(JournalEntry.JournalEntryStatus.POSTED);
        journalEntry.setCompany(company);
        journalEntry.setCreatedBy(user);

        // Find fiscal year
        Optional<FiscalYear> fiscalYear = fiscalYearService.findByDate(sale.getTransactionDate(), company);
        fiscalYear.ifPresent(journalEntry::setFiscalYear);

        // Create journal entry lines
        List<JournalEntryLine> lines = new ArrayList<>();

        // 1. Record COGS - Debit COGS (Expense increases)
        JournalEntryLine cogsDebitLine = new JournalEntryLine();
        cogsDebitLine.setJournalEntry(journalEntry);
        cogsDebitLine.setAccount(cogsAccount);
        cogsDebitLine.setDescription("Cost of goods sold");
        cogsDebitLine.setDebitAmount(totalCostAmount);
        cogsDebitLine.setCreditAmount(BigDecimal.ZERO);
        cogsDebitLine.setLineNumber(1);
        lines.add(cogsDebitLine);

        // 2. Credit Inventory (Asset decreases)
        JournalEntryLine inventoryCreditLine = new JournalEntryLine();
        inventoryCreditLine.setJournalEntry(journalEntry);
        inventoryCreditLine.setAccount(inventoryAccount);
        inventoryCreditLine.setDescription("Inventory reduction from sale");
        inventoryCreditLine.setDebitAmount(BigDecimal.ZERO);
        inventoryCreditLine.setCreditAmount(totalCostAmount);
        inventoryCreditLine.setLineNumber(2);
        lines.add(inventoryCreditLine);

        // 3. Debit Accounts Receivable/Cash (Asset increases)
        JournalEntryLine arDebitLine = new JournalEntryLine();
        arDebitLine.setJournalEntry(journalEntry);
        arDebitLine.setAccount(receivableAccount);
        arDebitLine.setDescription("AR".equals(accountingMethod) ? "Accounts Receivable from sale" : "Cash received from sale");
        arDebitLine.setDebitAmount(totalAmount);
        arDebitLine.setCreditAmount(BigDecimal.ZERO);
        arDebitLine.setLineNumber(3);
        lines.add(arDebitLine);

        // 4. Credit Sales Revenue (Revenue increases)
        JournalEntryLine revenueCreditLine = new JournalEntryLine();
        revenueCreditLine.setJournalEntry(journalEntry);
        revenueCreditLine.setAccount(revenueAccount);
        revenueCreditLine.setDescription("Sales revenue");
        revenueCreditLine.setDebitAmount(BigDecimal.ZERO);
        revenueCreditLine.setCreditAmount(totalAmount);
        revenueCreditLine.setLineNumber(4);
        lines.add(revenueCreditLine);

        journalEntry.setEntryLines(lines);

        // Save and return
        return journalEntryService.save(journalEntry);
    }

    private JournalEntry createAdjustmentJournalEntry(
            InventoryTransaction adjustment,
            User user,
            BigDecimal adjustmentValue) {

        Company company = adjustment.getCompany();

        // Find appropriate accounts
        Account inventoryAccount = getAccountByCode(INVENTORY_ACCOUNT_CODE, company);
        Account cogsAccount = getAccountByCode(COST_OF_GOODS_SOLD_ACCOUNT_CODE, company);

        // Create journal entry
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setReference("INV-" + adjustment.getReference());
        journalEntry.setEntryDate(adjustment.getTransactionDate());
        journalEntry.setDescription("Inventory adjustment: " + adjustment.getReference());
        journalEntry.setStatus(JournalEntry.JournalEntryStatus.POSTED);
        journalEntry.setCompany(company);
        journalEntry.setCreatedBy(user);

        // Find fiscal year
        Optional<FiscalYear> fiscalYear = fiscalYearService.findByDate(adjustment.getTransactionDate(), company);
        fiscalYear.ifPresent(journalEntry::setFiscalYear);

        // Create journal entry lines
        List<JournalEntryLine> lines = new ArrayList<>();

        if (adjustmentValue.compareTo(BigDecimal.ZERO) > 0) {
            // Positive adjustment (increase in inventory value)
            // Debit Inventory (Asset increases)
            JournalEntryLine debitLine = new JournalEntryLine();
            debitLine.setJournalEntry(journalEntry);
            debitLine.setAccount(inventoryAccount);
            debitLine.setDescription("Inventory positive adjustment");
            debitLine.setDebitAmount(adjustmentValue.abs());
            debitLine.setCreditAmount(BigDecimal.ZERO);
            debitLine.setLineNumber(1);
            lines.add(debitLine);

            // Credit COGS (Expense decreases, unusual but appropriate for positive adjustments)
            JournalEntryLine creditLine = new JournalEntryLine();
            creditLine.setJournalEntry(journalEntry);
            creditLine.setAccount(cogsAccount);
            creditLine.setDescription("COGS adjustment for inventory increase");
            creditLine.setDebitAmount(BigDecimal.ZERO);
            creditLine.setCreditAmount(adjustmentValue.abs());
            creditLine.setLineNumber(2);
            lines.add(creditLine);
        } else {
            // Negative adjustment (decrease in inventory value)
            // Debit COGS (Expense increases)
            JournalEntryLine debitLine = new JournalEntryLine();
            debitLine.setJournalEntry(journalEntry);
            debitLine.setAccount(cogsAccount);
            debitLine.setDescription("COGS adjustment for inventory decrease");
            debitLine.setDebitAmount(adjustmentValue.abs());
            debitLine.setCreditAmount(BigDecimal.ZERO);
            debitLine.setLineNumber(1);
            lines.add(debitLine);

            // Credit Inventory (Asset decreases)
            JournalEntryLine creditLine = new JournalEntryLine();
            creditLine.setJournalEntry(journalEntry);
            creditLine.setAccount(inventoryAccount);
            creditLine.setDescription("Inventory negative adjustment");
            creditLine.setDebitAmount(BigDecimal.ZERO);
            creditLine.setCreditAmount(adjustmentValue.abs());
            creditLine.setLineNumber(2);
            lines.add(creditLine);
        }

        journalEntry.setEntryLines(lines);

        // Save and return
        return journalEntryService.save(journalEntry);
    }

    private Account getAccountByCode(String code, Company company) {
        Optional<Account> account = accountService.findByCode(code, company);
        if (!account.isPresent()) {
            throw new IllegalStateException("Account with code " + code + " not found for this company");
        }
        return account.get();
    }
}