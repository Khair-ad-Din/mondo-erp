// src/main/java/com/mondo/erp/core/controller/finance/FiscalYearController.java
package com.mondo.erp.core.controller.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.finance.FiscalYear;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.finance.FiscalYearService;
import com.mondo.erp.core.service.finance.JournalEntryService;
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

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/finance/fiscal-years")
public class FiscalYearController {

    private final FiscalYearService fiscalYearService;
    private final JournalEntryService journalEntryService;
    private final UserService userService;

    @Autowired
    public FiscalYearController(
            FiscalYearService fiscalYearService,
            JournalEntryService journalEntryService,
            UserService userService) {
        this.fiscalYearService = fiscalYearService;
        this.journalEntryService = journalEntryService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_READ')")
    public String listFiscalYears(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        List<FiscalYear> fiscalYears = fiscalYearService.findAllByCompany(company);
        model.addAttribute("fiscalYears", fiscalYears);
        return "finance/fiscal-year/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('FINANCE_CREATE')")
    public String newFiscalYearForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        FiscalYear fiscalYear = new FiscalYear();
        fiscalYear.setStatus(FiscalYear.FiscalYearStatus.OPEN);

        // Set default dates for the new fiscal year (current year Jan 1 - Dec 31)
        int currentYear = LocalDate.now().getYear();
        fiscalYear.setStartDate(LocalDate.of(currentYear, 1, 1));
        fiscalYear.setEndDate(LocalDate.of(currentYear, 12, 31));

        model.addAttribute("fiscalYear", fiscalYear);
        return "finance/fiscal-year/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    public String editFiscalYearForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        FiscalYear fiscalYear = fiscalYearService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fiscal year ID: " + id));

        // Security check - ensure user can only edit fiscal years from their company
        if (!fiscalYear.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to edit this fiscal year");
        }

        model.addAttribute("fiscalYear", fiscalYear);
        return "finance/fiscal-year/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FINANCE_CREATE', 'FINANCE_UPDATE')")
    public String saveFiscalYear(
            @Valid @ModelAttribute("fiscalYear") FiscalYear fiscalYear,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Set company for new fiscal years
        if (fiscalYear.getCompany() == null) {
            fiscalYear.setCompany(company);
        }

        // Security check - ensure user can only modify fiscal years from their company
        if (!fiscalYear.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to modify this fiscal year");
        }

        // Validate fiscal year dates
        if (fiscalYear.getStartDate() != null && fiscalYear.getEndDate() != null) {
            if (fiscalYear.getStartDate().isAfter(fiscalYear.getEndDate())) {
                result.rejectValue("endDate", "invalid", "End date must be after start date");
            }
        }

        // Validate unique name
        if (!fiscalYearService.isNameUnique(fiscalYear.getName(), company, fiscalYear.getId())) {
            result.rejectValue("name", "duplicate", "Fiscal year name must be unique");
        }

        // Check for overlapping dates with other fiscal years
        List<FiscalYear> existingYears = fiscalYearService.findAllByCompany(company);
        for (FiscalYear existing : existingYears) {
            if (existing.getId().equals(fiscalYear.getId())) {
                continue; // Skip current fiscal year for update case
            }

            if ((fiscalYear.getStartDate().isBefore(existing.getEndDate()) || fiscalYear.getStartDate().isEqual(existing.getEndDate()))
                    && (fiscalYear.getEndDate().isAfter(existing.getStartDate()) || fiscalYear.getEndDate().isEqual(existing.getStartDate()))) {
                result.rejectValue("startDate", "overlap", "Fiscal year dates overlap with existing fiscal year");
                result.rejectValue("endDate", "overlap", "Fiscal year dates overlap with existing fiscal year");
                break;
            }
        }

        if (result.hasErrors()) {
            return "finance/fiscal-year/form";
        }

        fiscalYearService.save(fiscalYear);
        redirectAttributes.addFlashAttribute("successMessage", "Fiscal year saved successfully");
        return "redirect:/finance/fiscal-years";
    }

    @GetMapping("/{id}/close")
    @PreAuthorize("hasAuthority('FINANCE_UPDATE')")
    public String closeFiscalYear(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        try {
            FiscalYear fiscalYear = fiscalYearService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fiscal year ID: " + id));

            // Security check
            if (!fiscalYear.getCompany().getId().equals(company.getId())) {
                throw new SecurityException("Not authorized to close this fiscal year");
            }

            // Check if fiscal year is already closed
            if (fiscalYear.getStatus() == FiscalYear.FiscalYearStatus.CLOSED) {
                throw new IllegalStateException("Fiscal year is already closed");
            }

            // Close the fiscal year
            fiscalYear.setStatus(FiscalYear.FiscalYearStatus.CLOSED);
            fiscalYearService.save(fiscalYear);

            redirectAttributes.addFlashAttribute("successMessage", "Fiscal year closed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error closing fiscal year: " + e.getMessage());
        }

        return "redirect:/finance/fiscal-years";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('FINANCE_DELETE')")
    public String deleteFiscalYear(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        try {
            FiscalYear fiscalYear = fiscalYearService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid fiscal year ID: " + id));

            // Security check
            if (!fiscalYear.getCompany().getId().equals(company.getId())) {
                throw new SecurityException("Not authorized to delete this fiscal year");
            }

            // Check if the fiscal year has journal entries
            List<FiscalYear> existingYears = fiscalYearService.findAllByCompany(company);

            fiscalYearService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Fiscal year deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting fiscal year. It may have associated journal entries.");
        }

        return "redirect:/finance/fiscal-years";
    }
}