// src/main/java/com/mondo/erp/core/controller/finance/ReportController.java
package com.mondo.erp.core.controller.finance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.finance.Account;
import com.mondo.erp.core.model.finance.AccountType;
import com.mondo.erp.core.model.finance.FiscalYear;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.finance.AccountService;
import com.mondo.erp.core.service.finance.FiscalYearService;
import com.mondo.erp.core.service.finance.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/finance/reports")
public class ReportController {

    private final ReportService reportService;
    private final AccountService accountService;
    private final FiscalYearService fiscalYearService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ReportController(
            ReportService reportService,
            AccountService accountService,
            FiscalYearService fiscalYearService,
            UserService userService) {
        this.reportService = reportService;
        this.accountService = accountService;
        this.fiscalYearService = fiscalYearService;
        this.userService = userService;

        // Configure ObjectMapper for JSON serialization
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_REPORT')")
    public String reportIndex(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        List<Account> accounts = accountService.findActiveAccountsByCompany(company);
        List<FiscalYear> fiscalYears = fiscalYearService.findAllByCompany(company);

        model.addAttribute("accounts", accounts);
        model.addAttribute("fiscalYears", fiscalYears);

        // Serialize fiscal years for JavaScript
        try {
            List<Map<String, Object>> fiscalYearsData = new ArrayList<>();

            for (FiscalYear year : fiscalYears) {
                Map<String, Object> yearData = new HashMap<>();
                yearData.put("id", year.getId());
                yearData.put("name", year.getName());
                yearData.put("startDate", year.getStartDate().toString());
                yearData.put("endDate", year.getEndDate().toString());
                fiscalYearsData.add(yearData);
            }

            model.addAttribute("fiscalYearsJson", objectMapper.writeValueAsString(fiscalYearsData));
        } catch (JsonProcessingException e) {
            model.addAttribute("fiscalYearsJson", "[]");
        }

        return "finance/report/index";
    }

    @GetMapping("/balance-sheet")
    @PreAuthorize("hasAuthority('FINANCE_REPORT')")
    public String balanceSheet(
            Model model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long fiscalYearId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Default to current date if not specified
        if (date == null) {
            date = LocalDate.now();
        }

        // Get fiscal year if specified
        FiscalYear fiscalYear = null;
        if (fiscalYearId != null) {
            fiscalYear = fiscalYearService.findById(fiscalYearId).orElse(null);
        }

        // Calculate account balances
        Map<Account, BigDecimal> accountBalances = reportService.calculateBalanceSheetAccounts(company, date, fiscalYear);

        // Get accounts by category
        List<Account> currentAssets = accountService.findAllByCompany(company).stream()
                .filter(a -> a.getAccountType().getCategory() == AccountType.AccountCategory.ASSET)
                .filter(a -> a.getCode().startsWith("1") && !a.getCode().startsWith("15")) // Assuming 10xx-14xx are current assets
                .collect(Collectors.toList());

        List<Account> fixedAssets = accountService.findAllByCompany(company).stream()
                .filter(a -> a.getAccountType().getCategory() == AccountType.AccountCategory.ASSET)
                .filter(a -> a.getCode().startsWith("15")) // Assuming 15xx are fixed assets
                .collect(Collectors.toList());

        List<Account> currentLiabilities = accountService.findAllByCompany(company).stream()
                .filter(a -> a.getAccountType().getCategory() == AccountType.AccountCategory.LIABILITY)
                .filter(a -> a.getCode().startsWith("2") && !a.getCode().startsWith("25")) // Assuming 20xx-24xx are current liabilities
                .collect(Collectors.toList());

        List<Account> longTermLiabilities = accountService.findAllByCompany(company).stream()
                .filter(a -> a.getAccountType().getCategory() == AccountType.AccountCategory.LIABILITY)
                .filter(a -> a.getCode().startsWith("25")) // Assuming 25xx are long-term liabilities
                .collect(Collectors.toList());

        List<Account> equityAccounts = accountService.findAllByCompany(company).stream()
                .filter(a -> a.getAccountType().getCategory() == AccountType.AccountCategory.EQUITY)
                .collect(Collectors.toList());

        // Calculate totals
        BigDecimal currentAssetTotal = currentAssets.stream()
                .map(accountBalances::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fixedAssetTotal = fixedAssets.stream()
                .map(accountBalances::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal assetTotal = currentAssetTotal.add(fixedAssetTotal);

        BigDecimal currentLiabilityTotal = currentLiabilities.stream()
                .map(accountBalances::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal longTermLiabilityTotal = longTermLiabilities.stream()
                .map(accountBalances::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal liabilityTotal = currentLiabilityTotal.add(longTermLiabilityTotal);

        BigDecimal equityAccountTotal = equityAccounts.stream()
                .map(accountBalances::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate current earnings (net income for the period)
        LocalDate startDate;
        if (fiscalYear != null) {
            startDate = fiscalYear.getStartDate();
        } else {
            // If no fiscal year specified, use start of year
            startDate = LocalDate.of(date.getYear(), 1, 1);
        }

        BigDecimal currentEarnings = reportService.calculateCurrentEarnings(company, startDate, date, fiscalYear);

        BigDecimal equityTotal = equityAccountTotal.add(currentEarnings);
        BigDecimal liabilityEquityTotal = liabilityTotal.add(equityTotal);

        // Add data to model
        model.addAttribute("company", company);
        model.addAttribute("reportDate", date);
        model.addAttribute("fiscalYear", fiscalYear);
        model.addAttribute("accountBalances", accountBalances);

        model.addAttribute("currentAssets", currentAssets);
        model.addAttribute("fixedAssets", fixedAssets);
        model.addAttribute("currentLiabilities", currentLiabilities);
        model.addAttribute("longTermLiabilities", longTermLiabilities);
        model.addAttribute("equityAccounts", equityAccounts);

        model.addAttribute("currentAssetTotal", currentAssetTotal);
        model.addAttribute("fixedAssetTotal", fixedAssetTotal);
        model.addAttribute("assetTotal", assetTotal);
        model.addAttribute("currentLiabilityTotal", currentLiabilityTotal);
        model.addAttribute("longTermLiabilityTotal", longTermLiabilityTotal);
        model.addAttribute("liabilityTotal", liabilityTotal);
        model.addAttribute("currentEarnings", currentEarnings);
        model.addAttribute("equityTotal", equityTotal);
        model.addAttribute("liabilityEquityTotal", liabilityEquityTotal);

        return "finance/report/balance-sheet";
    }
}