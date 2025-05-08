package com.mondo.erp.core.controller.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.finance.Account;
import com.mondo.erp.core.service.CompanyService;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.finance.AccountService;
import com.mondo.erp.core.service.finance.AccountTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/finance/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountTypeService accountTypeService;
    private final UserService userService;
    private final CompanyService companyService;

    @Autowired
    public AccountController(AccountService accountService,
                             AccountTypeService accountTypeService,
                             UserService userService,
                             CompanyService companyService) {
        this.accountService = accountService;
        this.accountTypeService = accountTypeService;
        this.userService = userService;
        this.companyService = companyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_READ')")
    public String listAccounts(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        List<Account> accounts = accountService.findAllByCompany(company);
        model.addAttribute("accounts", accounts);
        return "finance/account/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('FINANCE_CREATE')")
    public String newAccountForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        model.addAttribute("account", new Account());
        model.addAttribute("accountTypes", accountTypeService.findAll());
        model.addAttribute("parentAccounts", accountService.findAllByCompany(company));

        return "finance/account/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    public String editAccountForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        Account account = accountService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid account ID: " + id));

        if (!account.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to edit this account");
        }

        model.addAttribute("account", account);
        model.addAttribute("accountTypes", accountTypeService.findAll());
        model.addAttribute("parentAccounts", accountService.findAllByCompany(company).stream()
                .filter(a -> !a.getId().equals(id))
                .toList());

        return "finance/account/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FINANCE_CREATE', 'FINANCE_UPDATE')")
    public String saveAccount(@Valid @ModelAttribute("account") Account account,
                              BindingResult result,
                              Model model,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        //Set company for new accounts
        if (account.getCompany() == null) {
            account.setCompany(company);
        }

        // Security check - ensure user can only edit accounts from their company
        if (!account.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to modify this account");
        }

        // Validate unique account code
        if (!accountService.isCodeUnique(account.getCode(), company, account.getId())) {
            result.rejectValue("code", "duplicate", "Account code must be unique");
        }

        if(result.hasErrors()) {
            model.addAttribute("accountTypes", accountTypeService.findAll());
            model.addAttribute("parentAccounts", accountService.findAllByCompany(company).stream()
                    .filter(a -> !a.getId().equals(account.getId()))// Exclude current account
                    .toList());

            return "finance/account/form";
        }

        accountService.save(account);
        redirectAttributes.addFlashAttribute("successMessage", "Account saved successfully");
        return "redirect:/finance/accounts";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('FINANCE_DELETE')")
    public String deleteAccount (@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        Account account = accountService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid account ID: " + id));

        // Security check - ensure user can only delete accounts from their company
        if (!account.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to delete this account");
        }

        // Check if account has children
        if (!account.getChildren().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete account with sub-accounts. Remove sub-accounts first.");
            return "redirect:/finance/accounts";
        }

        // TODO: Check if account is used in transactions

        accountService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully");
        return "redirect:/finance/accounts";
    }
}
