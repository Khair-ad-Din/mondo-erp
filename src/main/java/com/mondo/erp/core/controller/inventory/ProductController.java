// src/main/java/com/mondo/erp/core/controller/inventory/ProductController.java
package com.mondo.erp.core.controller.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.inventory.Product;
import com.mondo.erp.core.model.inventory.ProductCategory;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.inventory.ProductCategoryService;
import com.mondo.erp.core.service.inventory.ProductService;
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
@RequestMapping("/inventory/products")
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final UserService userService;

    @Autowired
    public ProductController(
            ProductService productService,
            ProductCategoryService categoryService,
            UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public String listProducts(
            Model model,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean showInactive,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Get all products for the company
        List<Product> allProducts = productService.findByCompany(company);

        // Filter products based on parameters
        List<Product> filteredProducts = allProducts.stream()
                .filter(p -> showInactive || p.isActive())
                .filter(p -> categoryId == null ||
                        (p.getCategory() != null && p.getCategory().getId().equals(categoryId)))
                .filter(p -> search == null || search.isEmpty() ||
                        p.getCode().toLowerCase().contains(search.toLowerCase()) ||
                        p.getName().toLowerCase().contains(search.toLowerCase()))
                .toList();

        // Get all categories for filter dropdown
        List<ProductCategory> categories = categoryService.findByCompany(company);

        model.addAttribute("products", filteredProducts);
        model.addAttribute("categories", categories);
        return "inventory/product/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public String newProductForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        Product product = new Product();
        product.setActive(true);
        product.setStockQuantity(0);

        prepareFormModel(model, product, company);
        return "inventory/product/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public String editProductForm(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        // Security check
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to edit this product");
        }

        prepareFormModel(model, product, company);
        return "inventory/product/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('INVENTORY_CREATE', 'INVENTORY_UPDATE')")
    public String saveProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Set company for new products
        if (product.getCompany() == null) {
            product.setCompany(company);
        }

        // Security check
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to modify this product");
        }

        // Validate unique code
        if (!productService.isCodeUnique(product.getCode(), company, product.getId())) {
            result.rejectValue("code", "duplicate", "Product code must be unique");
        }

        if (result.hasErrors()) {
            prepareFormModel(model, product, company);
            return "inventory/product/form";
        }

        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully");
        return "redirect:/inventory/products";
    }

    @GetMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public String deactivateProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        // Security check
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to modify this product");
        }

        product.setActive(false);
        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product deactivated successfully");
        return "redirect:/inventory/products";
    }

    @GetMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE')")
    public String activateProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        // Security check
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to modify this product");
        }

        product.setActive(true);
        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product activated successfully");
        return "redirect:/inventory/products";
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public String productHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        // Redirect to the movement history report for this specific product
        return "redirect:/inventory/reports/movement-history?productId=" + id;
    }

    private void prepareFormModel(Model model, Product product, Company company) {
        List<ProductCategory> categories = categoryService.findByCompany(company);
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
    }
}