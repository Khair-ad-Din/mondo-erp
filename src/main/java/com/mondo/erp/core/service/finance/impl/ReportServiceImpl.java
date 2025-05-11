// src/main/java/com/mondo/erp/core/service/finance/impl/ReportServiceImpl.java
package com.mondo.erp.core.service.finance.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.Account;
import com.mondo.erp.core.model.finance.AccountType;
import com.mondo.erp.core.model.finance.FiscalYear;
import com.mondo.erp.core.model.finance.JournalEntry;
import com.mondo.erp.core.model.finance.JournalEntryLine;
import com.mondo.erp.core.service.finance.AccountService;
import com.mondo.erp.core.service.finance.JournalEntryService;
import com.mondo.erp.core.service.finance.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final AccountService accountService;
    private final JournalEntryService journalEntryService;

    @Autowired
    public ReportServiceImpl(AccountService accountService, JournalEntryService journalEntryService) {
        this.accountService = accountService;
        this.journalEntryService = journalEntryService;
    }

    @Override
    public Map<Account, BigDecimal> calculateBalanceSheetAccounts(Company company, LocalDate asOfDate, FiscalYear fiscalYear) {
        List<Account> accounts = accountService.findAllByCompany(company);
        Map<Account, BigDecimal> balances = new HashMap<>();

        // Get all approved and posted journal entries up to the specified date
        List<JournalEntry> journalEntries = journalEntryService.findByCompany(company).stream()
                .filter(entry -> (entry.getStatus() == JournalEntry.JournalEntryStatus.APPROVED ||
                        entry.getStatus() == JournalEntry.JournalEntryStatus.POSTED) &&
                        !entry.getEntryDate().isAfter(asOfDate) &&
                        (fiscalYear == null || entry.getFiscalYear().getId().equals(fiscalYear.getId())))
                .collect(Collectors.toList());

        // Initialize balances
        for (Account account : accounts) {
            balances.put(account, BigDecimal.ZERO);
        }

        // Calculate balances
        for (JournalEntry entry : journalEntries) {
            for (JournalEntryLine line : entry.getEntryLines()) {
                Account account = line.getAccount();
                AccountType.AccountCategory category = account.getAccountType().getCategory();

                // For balance sheet accounts (ASSET, LIABILITY, EQUITY)
                if (category == AccountType.AccountCategory.ASSET ||
                        category == AccountType.AccountCategory.LIABILITY ||
                        category == AccountType.AccountCategory.EQUITY) {

                    BigDecimal currentBalance = balances.getOrDefault(account, BigDecimal.ZERO);

                    // Assets: Debit increases, Credit decreases
                    if (category == AccountType.AccountCategory.ASSET) {
                        currentBalance = currentBalance.add(line.getDebitAmount()).subtract(line.getCreditAmount());
                    }
                    // Liabilities and Equity: Credit increases, Debit decreases
                    else {
                        currentBalance = currentBalance.add(line.getCreditAmount()).subtract(line.getDebitAmount());
                    }

                    balances.put(account, currentBalance);
                }
            }
        }

        return balances;
    }

    @Override
    public BigDecimal calculateCurrentEarnings(Company company, LocalDate fromDate, LocalDate toDate, FiscalYear fiscalYear) {
        List<Account> accounts = accountService.findAllByCompany(company);
        Map<Account, BigDecimal> balances = new HashMap<>();

        // Get all approved and posted journal entries within the date range
        List<JournalEntry> journalEntries = journalEntryService.findByCompany(company).stream()
                .filter(entry -> (entry.getStatus() == JournalEntry.JournalEntryStatus.APPROVED ||
                        entry.getStatus() == JournalEntry.JournalEntryStatus.POSTED) &&
                        !entry.getEntryDate().isBefore(fromDate) &&
                        !entry.getEntryDate().isAfter(toDate) &&
                        (fiscalYear == null || entry.getFiscalYear().getId().equals(fiscalYear.getId())))
                .collect(Collectors.toList());

        // Initialize balances
        for (Account account : accounts) {
            balances.put(account, BigDecimal.ZERO);
        }

        // Calculate income and expense account balances
        for (JournalEntry entry : journalEntries) {
            for (JournalEntryLine line : entry.getEntryLines()) {
                Account account = line.getAccount();
                AccountType.AccountCategory category = account.getAccountType().getCategory();

                // For income statement accounts (REVENUE, EXPENSE)
                if (category == AccountType.AccountCategory.REVENUE ||
                        category == AccountType.AccountCategory.EXPENSE) {

                    BigDecimal currentBalance = balances.getOrDefault(account, BigDecimal.ZERO);

                    // Revenue: Credit increases, Debit decreases
                    if (category == AccountType.AccountCategory.REVENUE) {
                        currentBalance = currentBalance.add(line.getCreditAmount()).subtract(line.getDebitAmount());
                    }
                    // Expenses: Debit increases, Credit decreases
                    else {
                        currentBalance = currentBalance.add(line.getDebitAmount()).subtract(line.getCreditAmount());
                    }

                    balances.put(account, currentBalance);
                }
            }
        }

        // Calculate total revenue
        BigDecimal totalRevenue = accounts.stream()
                .filter(a -> a.getAccountType().getCategory() == AccountType.AccountCategory.REVENUE)
                .map(balances::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total expenses
        BigDecimal totalExpenses = accounts.stream()
                .filter(a -> a.getAccountType().getCategory() == AccountType.AccountCategory.EXPENSE)
                .map(balances::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Net income = Revenue - Expenses
        return totalRevenue.subtract(totalExpenses);
    }

    @Override
    public Map<Account, BigDecimal> calculateIncomeStatementAccounts(Company company, LocalDate fromDate, LocalDate toDate, FiscalYear fiscalYear) {
        List<Account> accounts = accountService.findAllByCompany(company);
        Map<Account, BigDecimal> balances = new HashMap<>();

        // Get all approved and posted journal entries within the date range
        List<JournalEntry> journalEntries = journalEntryService.findByCompany(company).stream()
                .filter(entry -> (entry.getStatus() == JournalEntry.JournalEntryStatus.APPROVED ||
                        entry.getStatus() == JournalEntry.JournalEntryStatus.POSTED) &&
                        !entry.getEntryDate().isBefore(fromDate) &&
                        !entry.getEntryDate().isAfter(toDate) &&
                        (fiscalYear == null || entry.getFiscalYear().getId().equals(fiscalYear.getId())))
                .collect(Collectors.toList());

        // Initialize balances
        for (Account account : accounts) {
            balances.put(account, BigDecimal.ZERO);
        }

        // Calculate income and expense account balances
        for (JournalEntry entry : journalEntries) {
            for (JournalEntryLine line : entry.getEntryLines()) {
                Account account = line.getAccount();
                AccountType.AccountCategory category = account.getAccountType().getCategory();

                // For income statement accounts (REVENUE, EXPENSE)
                if (category == AccountType.AccountCategory.REVENUE ||
                        category == AccountType.AccountCategory.EXPENSE) {

                    BigDecimal currentBalance = balances.getOrDefault(account, BigDecimal.ZERO);

                    // Revenue: Credit increases, Debit decreases
                    if (category == AccountType.AccountCategory.REVENUE) {
                        currentBalance = currentBalance.add(line.getCreditAmount()).subtract(line.getDebitAmount());
                    }
                    // Expenses: Debit increases, Credit decreases
                    else {
                        currentBalance = currentBalance.add(line.getDebitAmount()).subtract(line.getCreditAmount());
                    }

                    balances.put(account, currentBalance);
                }
            }
        }

        return balances;
    }

    @Override
    public Map<Account, Object> calculateGeneralLedger(Company company, LocalDate fromDate, LocalDate toDate, Account specificAccount) {
        List<Account> accounts;

        if (specificAccount != null) {
            accounts = List.of(specificAccount);
        } else {
            accounts = accountService.findAllByCompany(company);
        }

        Map<Account, Object> ledger = new HashMap<>();

        // Get all approved and posted journal entries within the date range
        List<JournalEntry> journalEntries = journalEntryService.findByCompany(company).stream()
                .filter(entry -> (entry.getStatus() == JournalEntry.JournalEntryStatus.APPROVED ||
                        entry.getStatus() == JournalEntry.JournalEntryStatus.POSTED) &&
                        !entry.getEntryDate().isAfter(toDate))
                .sorted(Comparator.comparing(JournalEntry::getEntryDate))
                .collect(Collectors.toList());

        // Calculate starting balances and transactions for each account
        for (Account account : accounts) {
            Map<String, Object> accountLedger = new HashMap<>();
            List<Map<String, Object>> transactions = new ArrayList<>();

            // Calculate starting balance (all transactions before fromDate)
            BigDecimal startingBalance = BigDecimal.ZERO;
            AccountType.AccountCategory category = account.getAccountType().getCategory();

            // Process journal entries
            for (JournalEntry entry : journalEntries) {
                for (JournalEntryLine line : entry.getEntryLines()) {
                    if (!line.getAccount().getId().equals(account.getId())) {
                        continue;
                    }

                    BigDecimal debitAmount = line.getDebitAmount();
                    BigDecimal creditAmount = line.getCreditAmount();
                    BigDecimal netAmount;

                    // Determine effect on balance based on account type
                    if (category == AccountType.AccountCategory.ASSET ||
                            category == AccountType.AccountCategory.EXPENSE) {
                        // Debit increases, Credit decreases
                        netAmount = debitAmount.subtract(creditAmount);
                    } else {
                        // Credit increases, Debit decreases for LIABILITY, EQUITY, REVENUE
                        netAmount = creditAmount.subtract(debitAmount);
                    }

                    // Add to starting balance if before fromDate
                    if (entry.getEntryDate().isBefore(fromDate)) {
                        startingBalance = startingBalance.add(netAmount);
                    }
                    // Add as transaction if within date range
                    else {
                        Map<String, Object> transaction = new HashMap<>();
                        transaction.put("date", entry.getEntryDate());
                        transaction.put("reference", entry.getReference());
                        transaction.put("description", line.getDescription());
                        transaction.put("debit", debitAmount);
                        transaction.put("credit", creditAmount);
                        transaction.put("runningBalance", BigDecimal.ZERO); // Will calculate later

                        transactions.add(transaction);
                    }
                }
            }

            // Calculate running balance
            BigDecimal runningBalance = startingBalance;
            for (Map<String, Object> transaction : transactions) {
                BigDecimal debit = (BigDecimal) transaction.get("debit");
                BigDecimal credit = (BigDecimal) transaction.get("credit");

                if (category == AccountType.AccountCategory.ASSET ||
                        category == AccountType.AccountCategory.EXPENSE) {
                    runningBalance = runningBalance.add(debit).subtract(credit);
                } else {
                    runningBalance = runningBalance.add(credit).subtract(debit);
                }

                transaction.put("runningBalance", runningBalance);
            }

            // Build account ledger
            accountLedger.put("account", account);
            accountLedger.put("startingBalance", startingBalance);
            accountLedger.put("transactions", transactions);
            accountLedger.put("endingBalance", runningBalance);

            ledger.put(account, accountLedger);
        }

        return ledger;
    }

    // Helper method for imports
    private static class ArrayList<T> extends java.util.ArrayList<T> {
        private static final long serialVersionUID = 1L;
    }
}