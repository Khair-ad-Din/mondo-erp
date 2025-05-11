// src/main/java/com/mondo/erp/core/service/finance/ReportService.java
package com.mondo.erp.core.service.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.Account;
import com.mondo.erp.core.model.finance.FiscalYear;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface ReportService {

    /**
     * Calculates account balances for a balance sheet as of a specific date
     *
     * @param company The company
     * @param asOfDate The balance sheet date
     * @param fiscalYear Optional fiscal year to filter entries
     * @return Map of accounts to their balances
     */
    Map<Account, BigDecimal> calculateBalanceSheetAccounts(Company company, LocalDate asOfDate, FiscalYear fiscalYear);

    /**
     * Calculates current earnings (net income) for a period
     *
     * @param company The company
     * @param fromDate Start date for period
     * @param toDate End date for period
     * @param fiscalYear Optional fiscal year to filter entries
     * @return Net income for the period
     */
    BigDecimal calculateCurrentEarnings(Company company, LocalDate fromDate, LocalDate toDate, FiscalYear fiscalYear);

    /**
     * Calculates income statement accounts for a period
     *
     * @param company The company
     * @param fromDate Start date for period
     * @param toDate End date for period
     * @param fiscalYear Optional fiscal year to filter entries
     * @return Map of accounts to their balances
     */
    Map<Account, BigDecimal> calculateIncomeStatementAccounts(Company company, LocalDate fromDate, LocalDate toDate, FiscalYear fiscalYear);

    /**
     * Calculate general ledger for accounts, showing starting balance, movements, and ending balance
     *
     * @param company The company
     * @param fromDate Start date for period
     * @param toDate End date for period
     * @param account Optional account to filter (null for all accounts)
     * @return General ledger data structure
     */
    Map<Account, Object> calculateGeneralLedger(Company company, LocalDate fromDate, LocalDate toDate, Account account);
}