// src/main/java/com/mondo/erp/core/controller/finance/JournalEntryController.java
package com.mondo.erp.core.controller.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.finance.Account;
import com.mondo.erp.core.model.finance.FiscalYear;
import com.mondo.erp.core.model.finance.JournalEntry;
import com.mondo.erp.core.model.finance.JournalEntryLine;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.finance.AccountService;
import com.mondo.erp.core.service.finance.FiscalYearService;
import com.mondo.erp.core.service.finance.JournalEntryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/finance/journal-entries")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;
    private final AccountService accountService;
    private final FiscalYearService fiscalYearService;
    private final UserService userService;

    @Autowired
    public JournalEntryController(
            JournalEntryService journalEntryService,
            AccountService accountService,
            FiscalYearService fiscalYearService,
            UserService userService) {
        this.journalEntryService = journalEntryService;
        this.accountService = accountService;
        this.fiscalYearService = fiscalYearService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_READ')")
    public String listJournalEntries(
            Model model,
            @RequestParam(required = false) JournalEntry.JournalEntryStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        List<JournalEntry> journalEntries;

        if (status != null) {
            journalEntries = journalEntryService.findByStatusAndCompany(status, company);
        } else if (fromDate != null && toDate != null) {
            journalEntries = journalEntryService.findByCompanyAndEntryDateBetween(company, fromDate, toDate);
        } else {
            journalEntries = journalEntryService.findByCompany(company);
        }

        model.addAttribute("journalEntries", journalEntries);
        return "finance/journal-entry/list";
    }

    @GetMapping("/{id}/view")
    @PreAuthorize("hasAuthority('FINANCE_READ')")
    public String viewJournalEntry(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        JournalEntry journalEntry = journalEntryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid journal entry ID: " + id));

        // Security check - ensure user can only view entries from their company
        if (!journalEntry.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to view this journal entry");
        }

        model.addAttribute("journalEntry", journalEntry);
        return "finance/journal-entry/view";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('FINANCE_CREATE')")
    public String newJournalEntryForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setEntryDate(LocalDate.now());
        journalEntry.setStatus(JournalEntry.JournalEntryStatus.DRAFT);
        journalEntry.setCompany(company);
        journalEntry.setReference(journalEntryService.generateNextReference(company));

        // Add an initial empty line
        JournalEntryLine line = new JournalEntryLine();
        line.setLineNumber(1);
        journalEntry.getEntryLines().add(line);

        prepareFormModel(model, journalEntry, company);
        return "finance/journal-entry/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    public String editJournalEntryForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        JournalEntry journalEntry = journalEntryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid journal entry ID: " + id));

        // Security check - ensure user can only edit entries from their company
        if (!journalEntry.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to edit this journal entry");
        }

        // Only DRAFT entries can be edited
        if (journalEntry.getStatus() != JournalEntry.JournalEntryStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT journal entries can be edited");
        }

        prepareFormModel(model, journalEntry, company);
        return "finance/journal-entry/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FINANCE_CREATE', 'FINANCE_UPDATE')")
    public String saveJournalEntry(
            @Valid @ModelAttribute("journalEntry") JournalEntry journalEntry,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Set company for new entries
        if (journalEntry.getCompany() == null) {
            journalEntry.setCompany(company);
        }

        // Security check - ensure user can only modify entries from their company
        if (!journalEntry.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to modify this journal entry");
        }

        // Validate unique reference
        if (!journalEntryService.isReferenceUnique(journalEntry.getReference(), company, journalEntry.getId())) {
            result.rejectValue("reference", "duplicate", "Journal entry reference must be unique");
        }

        // Check if the entry is balanced
        if (!journalEntry.isBalanced()) {
            result.rejectValue("entryLines", "unbalanced", "Journal entry must be balanced (debit total must equal credit total)");
        }

        if (result.hasErrors()) {
            prepareFormModel(model, journalEntry, company);
            return "finance/journal-entry/form";
        }

        journalEntryService.save(journalEntry);
        redirectAttributes.addFlashAttribute("successMessage", "Journal entry saved successfully");
        return "redirect:/finance/journal-entries";
    }

    @GetMapping("/{id}/post")
    @PreAuthorize("hasAuthority('FINANCE_POST')")
    public String postJournalEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        try {
            JournalEntry journalEntry = journalEntryService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid journal entry ID: " + id));

            // Security check
            if (!journalEntry.getCompany().getId().equals(company.getId())) {
                throw new SecurityException("Not authorized to post this journal entry");
            }

            journalEntryService.postJournalEntry(journalEntry, user);
            redirectAttributes.addFlashAttribute("successMessage", "Journal entry posted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error posting journal entry: " + e.getMessage());
        }

        return "redirect:/finance/journal-entries";
    }

    @GetMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('FINANCE_POST')")
    public String approveJournalEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        try {
            JournalEntry journalEntry = journalEntryService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid journal entry ID: " + id));

            // Security check
            if (!journalEntry.getCompany().getId().equals(company.getId())) {
                throw new SecurityException("Not authorized to approve this journal entry");
            }

            journalEntryService.approveJournalEntry(journalEntry, user);
            redirectAttributes.addFlashAttribute("successMessage", "Journal entry approved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving journal entry: " + e.getMessage());
        }

        return "redirect:/finance/journal-entries";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('FINANCE_DELETE')")
    public String deleteJournalEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        try {
            JournalEntry journalEntry = journalEntryService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid journal entry ID: " + id));

            // Security check
            if (!journalEntry.getCompany().getId().equals(company.getId())) {
                throw new SecurityException("Not authorized to delete this journal entry");
            }

            // Only DRAFT entries can be deleted
            if (journalEntry.getStatus() != JournalEntry.JournalEntryStatus.DRAFT) {
                throw new IllegalStateException("Only DRAFT journal entries can be deleted");
            }

            journalEntryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Journal entry deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting journal entry: " + e.getMessage());
        }

        return "redirect:/finance/journal-entries";
    }

    private void prepareFormModel(Model model, JournalEntry journalEntry, Company company) {
        List<Account> accounts = accountService.findActiveAccountsByCompany(company);
        List<FiscalYear> fiscalYears = fiscalYearService.findOpenFiscalYears(company);

        model.addAttribute("journalEntry", journalEntry);
        model.addAttribute("accounts", accounts);
        model.addAttribute("fiscalYears", fiscalYears);
    }
}