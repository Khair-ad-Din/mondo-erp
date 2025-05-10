// src/main/java/com/mondo/erp/core/controller/CompanyController.java
package com.mondo.erp.core.controller;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/companies")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public String listCompanies(Model model) {
        List<Company> companies = companyService.findAll();
        model.addAttribute("companies", companies);
        return "admin/company/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public String newCompanyForm(Model model) {
        model.addAttribute("company", new Company());
        return "admin/company/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public String editCompanyForm(@PathVariable Long id, Model model) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company ID: " + id));
        model.addAttribute("company", company);
        return "admin/company/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'USER_UPDATE')")
    public String saveCompany(@Valid @ModelAttribute("company") Company company,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {

        // Validate unique taxId
        if (company.getId() == null && companyService.existsByTaxId(company.getTaxId())) {
            result.rejectValue("taxId", "duplicate", "Tax ID must be unique");
        }

        if (result.hasErrors()) {
            return "admin/company/form";
        }

        companyService.save(company);
        redirectAttributes.addFlashAttribute("successMessage", "Company saved successfully");
        return "redirect:/admin/companies";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public String deleteCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            companyService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Company deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete company. It may have associated records.");
        }
        return "redirect:/admin/companies";
    }
}