// src/main/java/com/mondo/erp/core/controller/inventory/InventoryTransactionController.java
package com.mondo.erp.core.controller.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.inventory.InventoryTransaction;
import com.mondo.erp.core.model.inventory.InventoryTransactionLine;
import com.mondo.erp.core.model.inventory.Product;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.inventory.InventoryTransactionService;
import com.mondo.erp.core.service.inventory.ProductService;
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
@RequestMapping("/inventory/transactions")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public InventoryTransactionController(
            InventoryTransactionService transactionService,
            ProductService productService,
            UserService userService) {
        this.transactionService = transactionService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public String listTransactions(
            Model model,
            @RequestParam(required = false) InventoryTransaction.TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        List<InventoryTransaction> transactions;

        // Filter by type if specified
        if (type != null) {
            transactions = transactionService.findByCompanyAndType(company, type);
        }
        // Filter by date range if specified
        else if (fromDate != null && toDate != null) {
            transactions = transactionService.findByCompanyAndDateRange(company, fromDate, toDate);
        }
        // Otherwise, get all transactions
        else {
            transactions = transactionService.findByCompany(company);
        }

        model.addAttribute("transactions", transactions);
        return "inventory/transaction/list";
    }

    @GetMapping("/{id}/view")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public String viewTransaction(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        InventoryTransaction transaction = transactionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid transaction ID: " + id));

        // Security check
        if (!transaction.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized to view this transaction");
        }

        model.addAttribute("transaction", transaction);
        return "inventory/transaction/view";
    }

    // Purchase Transaction Methods

    @GetMapping("/purchase/new")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public String newPurchaseForm(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Create new purchase transaction
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setType(InventoryTransaction.TransactionType.PURCHASE);
        transaction.setTransactionDate(LocalDate.now());
        transaction.setCompany(company);

        // Generate reference number
        String reference = transactionService.generateNextReference(company, InventoryTransaction.TransactionType.PURCHASE);
        transaction.setReference(reference);

        // Add an initial empty line
        InventoryTransactionLine line = new InventoryTransactionLine();
        line.setQuantity(1);
        transaction.getTransactionLines().add(line);

        // Prepare model
        List<Product> activeProducts = productService.findByCompanyAndActive(company, true);

        model.addAttribute("transaction", transaction);
        model.addAttribute("products", activeProducts);

        return "inventory/transaction/purchase-form";
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public String savePurchase(
            @Valid @ModelAttribute("transaction") InventoryTransaction transaction,
            BindingResult result,
            @RequestParam("accountingMethod") String accountingMethod,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Set company
        if (transaction.getCompany() == null) {
            transaction.setCompany(company);
        }

        // Security check
        if (!transaction.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized for this company");
        }

        // Validate transaction type
        if (transaction.getType() != InventoryTransaction.TransactionType.PURCHASE) {
            result.rejectValue("type", "invalid", "Transaction type must be PURCHASE");
        }

        // Validate unique reference
        if (!transactionService.isReferenceUnique(transaction.getReference(), company, transaction.getId())) {
            result.rejectValue("reference", "duplicate", "Transaction reference must be unique");
        }

        // Validate lines
        if (transaction.getTransactionLines().isEmpty()) {
            result.rejectValue("transactionLines", "empty", "At least one product is required");
        } else {
            // Check each line for validity
            for (int i = 0; i < transaction.getTransactionLines().size(); i++) {
                InventoryTransactionLine line = transaction.getTransactionLines().get(i);

                if (line.getProduct() == null) {
                    result.rejectValue("transactionLines[" + i + "].product", "required", "Product is required");
                }

                if (line.getQuantity() == null || line.getQuantity() <= 0) {
                    result.rejectValue("transactionLines[" + i + "].quantity", "positive", "Quantity must be positive");
                }

                if (line.getUnitPrice() == null || line.getUnitPrice().signum() <= 0) {
                    result.rejectValue("transactionLines[" + i + "].unitPrice", "positive", "Unit price must be positive");
                }
            }
        }

        if (result.hasErrors()) {
            List<Product> activeProducts = productService.findByCompanyAndActive(company, true);
            model.addAttribute("products", activeProducts);
            return "inventory/transaction/purchase-form";
        }

        try {
            // Process the purchase
            transactionService.createPurchase(transaction, accountingMethod, user);
            redirectAttributes.addFlashAttribute("successMessage", "Purchase transaction created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating purchase: " + e.getMessage());
        }

        return "redirect:/inventory/transactions";
    }

    // Sale Transaction Methods

    @GetMapping("/sale/new")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public String newSaleForm(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Create new sale transaction
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setType(InventoryTransaction.TransactionType.SALE);
        transaction.setTransactionDate(LocalDate.now());
        transaction.setCompany(company);

        // Generate reference number
        String reference = transactionService.generateNextReference(company, InventoryTransaction.TransactionType.SALE);
        transaction.setReference(reference);

        // Add an initial empty line
        InventoryTransactionLine line = new InventoryTransactionLine();
        line.setQuantity(1);
        transaction.getTransactionLines().add(line);

        // Prepare model
        List<Product> activeProducts = productService.findByCompanyAndActive(company, true);

        model.addAttribute("transaction", transaction);
        model.addAttribute("products", activeProducts);

        return "inventory/transaction/sale-form";
    }

    @PostMapping("/sale")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public String saveSale(
            @Valid @ModelAttribute("transaction") InventoryTransaction transaction,
            BindingResult result,
            @RequestParam("accountingMethod") String accountingMethod,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Set company
        if (transaction.getCompany() == null) {
            transaction.setCompany(company);
        }

        // Security check
        if (!transaction.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized for this company");
        }

        // Validate transaction type
        if (transaction.getType() != InventoryTransaction.TransactionType.SALE) {
            result.rejectValue("type", "invalid", "Transaction type must be SALE");
        }

        // Validate unique reference
        if (!transactionService.isReferenceUnique(transaction.getReference(), company, transaction.getId())) {
            result.rejectValue("reference", "duplicate", "Transaction reference must be unique");
        }

        // Validate lines
        if (transaction.getTransactionLines().isEmpty()) {
            result.rejectValue("transactionLines", "empty", "At least one product is required");
        } else {
            // Check each line for validity and stock availability
            for (int i = 0; i < transaction.getTransactionLines().size(); i++) {
                InventoryTransactionLine line = transaction.getTransactionLines().get(i);

                if (line.getProduct() == null) {
                    result.rejectValue("transactionLines[" + i + "].product", "required", "Product is required");
                    continue;
                }

                if (line.getQuantity() == null || line.getQuantity() <= 0) {
                    result.rejectValue("transactionLines[" + i + "].quantity", "positive", "Quantity must be positive");
                    continue;
                }

                if (line.getUnitPrice() == null || line.getUnitPrice().signum() <= 0) {
                    result.rejectValue("transactionLines[" + i + "].unitPrice", "positive", "Unit price must be positive");
                    continue;
                }

                // Check stock availability
                Product product = line.getProduct();
                if (product.getStockQuantity() < line.getQuantity()) {
                    result.rejectValue("transactionLines[" + i + "].quantity", "stock",
                            "Not enough stock available. Current stock: " + product.getStockQuantity());
                }
            }
        }

        if (result.hasErrors()) {
            List<Product> activeProducts = productService.findByCompanyAndActive(company, true);
            model.addAttribute("products", activeProducts);
            return "inventory/transaction/sale-form";
        }

        try {
            // Process the sale
            transactionService.createSale(transaction, accountingMethod, user);
            redirectAttributes.addFlashAttribute("successMessage", "Sale transaction created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating sale: " + e.getMessage());
        }

        return "redirect:/inventory/transactions";
    }

    // Adjustment Transaction Methods

    @GetMapping("/adjustment/new")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public String newAdjustmentForm(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Create new adjustment transaction
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setType(InventoryTransaction.TransactionType.ADJUSTMENT);
        transaction.setTransactionDate(LocalDate.now());
        transaction.setCompany(company);

        // Generate reference number
        String reference = transactionService.generateNextReference(company, InventoryTransaction.TransactionType.ADJUSTMENT);
        transaction.setReference(reference);

        // Add an initial empty line
        InventoryTransactionLine line = new InventoryTransactionLine();
        transaction.getTransactionLines().add(line);

        // Prepare model
        List<Product> allProducts = productService.findByCompany(company);

        model.addAttribute("transaction", transaction);
        model.addAttribute("products", allProducts);

        return "inventory/transaction/adjustment-form";
    }

    @PostMapping("/adjustment")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE')")
    public String saveAdjustment(
            @Valid @ModelAttribute("transaction") InventoryTransaction transaction,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Set company
        if (transaction.getCompany() == null) {
            transaction.setCompany(company);
        }

        // Security check
        if (!transaction.getCompany().getId().equals(company.getId())) {
            throw new SecurityException("Not authorized for this company");
        }

        // Validate transaction type
        if (transaction.getType() != InventoryTransaction.TransactionType.ADJUSTMENT) {
            result.rejectValue("type", "invalid", "Transaction type must be ADJUSTMENT");
        }

        // Validate unique reference
        if (!transactionService.isReferenceUnique(transaction.getReference(), company, transaction.getId())) {
            result.rejectValue("reference", "duplicate", "Transaction reference must be unique");
        }

        // Validate lines
        if (transaction.getTransactionLines().isEmpty()) {
            result.rejectValue("transactionLines", "empty", "At least one product is required");
        } else {
            // Check each line for validity
            for (int i = 0; i < transaction.getTransactionLines().size(); i++) {
                InventoryTransactionLine line = transaction.getTransactionLines().get(i);

                if (line.getProduct() == null) {
                    result.rejectValue("transactionLines[" + i + "].product", "required", "Product is required");
                }

                if (line.getQuantity() == null || line.getQuantity() < 0) {
                    result.rejectValue("transactionLines[" + i + "].quantity", "negative", "Quantity cannot be negative");
                }
            }
        }

        if (result.hasErrors()) {
            List<Product> allProducts = productService.findByCompany(company);
            model.addAttribute("products", allProducts);
            return "inventory/transaction/adjustment-form";
        }

        try {
            // Process the adjustment
            transactionService.createAdjustment(transaction, user);
            redirectAttributes.addFlashAttribute("successMessage", "Inventory adjustment created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating adjustment: " + e.getMessage());
        }

        return "redirect:/inventory/transactions";
    }
}