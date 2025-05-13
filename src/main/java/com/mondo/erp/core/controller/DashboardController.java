// src/main/java/com/mondo/erp/core/controller/DashboardController.java
package com.mondo.erp.core.controller;

import com.mondo.erp.core.service.CompanyService;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.finance.JournalEntryService;
import com.mondo.erp.core.service.inventory.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final CompanyService companyService;
    private final JournalEntryService journalEntryService;
    private final ProductService productService;

    @Autowired
    public DashboardController(
            UserService userService,
            CompanyService companyService,
            JournalEntryService journalEntryService,
            ProductService productService) {
        this.userService = userService;
        this.companyService = companyService;
        this.journalEntryService = journalEntryService;
        this.productService = productService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/admindashboard")
    public String adminDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Add counts for admin dashboard stats
        long userCount = userService.countAll();
        long companyCount = companyService.countAll();
        long journalEntryCount = journalEntryService.countAll();
        long productCount = productService.countAll();

        model.addAttribute("userCount", userCount);
        model.addAttribute("companyCount", companyCount);
        model.addAttribute("journalEntryCount", journalEntryCount);
        model.addAttribute("productCount", productCount);

        return "dashboard/admin";
    }
}