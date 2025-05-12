// src/main/java/com/mondo/erp/core/controller/inventory/ProductCategoryController.java
package com.mondo.erp.core.controller.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.inventory.ProductCategory;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.inventory.ProductCategoryService;
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
@RequestMapping("/inventory/categories")
public class ProductCategoryController {

    private final ProductCategoryService categoryService;
    private final UserService userService;

    @Autowired
    public ProductCategoryController(
            ProductCategoryService categoryService,
            UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public String listCategories(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        List<ProductCategory> categories = categoryService.findByCompany(company);
        model.addAttribute("categories", categories);
        return "inventory/category/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new ProductCategory());
        return "inventory/category/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public String editCategoryForm(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        ProductCategory category = categoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + id));

        // Security check
        if (!category.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to edit this category");
        }

        model.addAttribute("category", category);
        return "inventory/category/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('INVENTORY_CREATE', 'INVENTORY_UPDATE')")
    public String saveCategory(
            @Valid @ModelAttribute("category") ProductCategory category,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Set company for new categories
        if (category.getCompany() == null) {
            category.setCompany(company);
        }

        // Security check
        if (!category.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to modify this category");
        }

        // Validate unique name
        if (!categoryService.isNameUnique(category.getName(), company, category.getId())) {
            result.rejectValue("name", "duplicate", "Category name must be unique");
        }

        if (result.hasErrors()) {
            return "inventory/category/form";
        }

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("successMessage", "Category saved successfully");
        return "redirect:/inventory/categories";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('INVENTORY_DELETE')")
    public String deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        try {
            ProductCategory category = categoryService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + id));

            // Security check
            if (!category.getCompany().getId().equals(company.getId())) {
                throw new SecurityException("Not authorized to delete this category");
            }

            // Check if category has products
            if (!category.getProducts().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Cannot delete category that has products. Please reassign products first.");
                return "redirect:/inventory/categories";
            }

            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting category: " + e.getMessage());
        }

        return "redirect:/inventory/categories";
    }
}