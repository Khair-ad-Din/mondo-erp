// src/main/java/com/mondo/erp/core/controller/inventory/InventoryReportController.java
package com.mondo.erp.core.controller.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.inventory.InventoryTransaction;
import com.mondo.erp.core.model.inventory.InventoryTransactionLine;
import com.mondo.erp.core.model.inventory.Product;
import com.mondo.erp.core.model.inventory.ProductCategory;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.inventory.InventoryTransactionService;
import com.mondo.erp.core.service.inventory.ProductCategoryService;
import com.mondo.erp.core.service.inventory.ProductService;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory/reports")
public class InventoryReportController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final InventoryTransactionService transactionService;
    private final UserService userService;

    @Autowired
    public InventoryReportController(
            ProductService productService,
            ProductCategoryService categoryService,
            InventoryTransactionService transactionService,
            UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public String reportIndex(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        List<Product> products = productService.findByCompany(company);
        List<ProductCategory> categories = categoryService.findByCompany(company);

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);

        return "inventory/report/index";
    }

    @GetMapping("/stock-level")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public String stockLevelReport(
            Model model,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean showInactive,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Get all categories for the filter dropdown
        List<ProductCategory> categories = categoryService.findByCompany(company);
        model.addAttribute("categories", categories);

        // Filter products based on parameters
        List<Product> allProducts = productService.findByCompany(company);
        List<Product> filteredProducts = allProducts.stream()
                .filter(p -> showInactive || p.isActive())
                .filter(p -> categoryId == null ||
                        (p.getCategory() != null && p.getCategory().getId().equals(categoryId)))
                .collect(Collectors.toList());

        // Calculate totals
        BigDecimal totalCostValue = filteredProducts.stream()
                .map(p -> p.getCostPrice().multiply(new BigDecimal(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaleValue = filteredProducts.stream()
                .map(p -> p.getSalePrice().multiply(new BigDecimal(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("products", filteredProducts);
        model.addAttribute("totalCostValue", totalCostValue);
        model.addAttribute("totalSaleValue", totalSaleValue);

        return "inventory/report/stock-level";
    }

    @GetMapping("/movement-history")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public String movementHistoryReport(
            Model model,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Company company = user.getCompany();

        // Get all products for the filter dropdown
        List<Product> allProducts = productService.findByCompany(company);
        model.addAttribute("allProducts", allProducts);

        // Set default dates if not provided
        if (fromDate == null) {
            // Default to 30 days ago
            fromDate = LocalDate.now().minusDays(30);
        }

        if (toDate == null) {
            toDate = LocalDate.now();
        }

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        // Get selected product if specified
        Product selectedProduct = null;
        if (productId != null) {
            selectedProduct = productService.findById(productId).orElse(null);
            model.addAttribute("product", selectedProduct);
        }

        // Get transactions
        List<InventoryTransaction> transactions = transactionService.findByCompanyAndDateRange(
                company, fromDate, toDate);

        // Build movement history
        List<MovementHistoryDTO> movements = buildMovementHistory(transactions, selectedProduct);

        // Calculate starting and ending balance if a product is selected
        if (selectedProduct != null) {
            // Starting balance is the current stock minus all movements in the period
            int currentStock = selectedProduct.getStockQuantity();
            int netMovement = movements.stream()
                    .mapToInt(m -> m.getQuantityIn() - m.getQuantityOut())
                    .sum();
            int startingBalance = currentStock - netMovement;
            int endingBalance = startingBalance + netMovement;

            model.addAttribute("startingBalance", startingBalance);
            model.addAttribute("endingBalance", endingBalance);
        }

        model.addAttribute("movements", movements);

        return "inventory/report/movement-history";
    }

    private List<MovementHistoryDTO> buildMovementHistory(
            List<InventoryTransaction> transactions, Product selectedProduct) {

        List<MovementHistoryDTO> movements = new ArrayList<>();

        // Running balances for each product
        Map<Long, Integer> productBalances = new HashMap<>();

        // Sort transactions by date
        transactions.sort(Comparator.comparing(InventoryTransaction::getTransactionDate));

        for (InventoryTransaction transaction : transactions) {
            for (InventoryTransactionLine line : transaction.getTransactionLines()) {
                // Skip if we're filtering for a specific product and this isn't it
                if (selectedProduct != null && !line.getProduct().getId().equals(selectedProduct.getId())) {
                    continue;
                }

                Product product = line.getProduct();
                Integer balance = productBalances.getOrDefault(product.getId(), 0);

                int quantityIn = 0;
                int quantityOut = 0;

                // Determine if this is in or out based on transaction type
                if (transaction.getType() == InventoryTransaction.TransactionType.PURCHASE) {
                    quantityIn = line.getQuantity();
                    balance += quantityIn;
                } else if (transaction.getType() == InventoryTransaction.TransactionType.SALE) {
                    quantityOut = line.getQuantity();
                    balance -= quantityOut;
                } else if (transaction.getType() == InventoryTransaction.TransactionType.ADJUSTMENT) {
                    // For adjustments, we need to calculate the difference
                    int oldBalance = balance;
                    balance = line.getQuantity(); // Adjustment sets absolute quantity

                    if (balance > oldBalance) {
                        quantityIn = balance - oldBalance;
                    } else {
                        quantityOut = oldBalance - balance;
                    }
                }

                // Update running balance for this product
                productBalances.put(product.getId(), balance);

                // Create movement history item
                MovementHistoryDTO movement = new MovementHistoryDTO();
                movement.setDate(transaction.getTransactionDate());
                movement.setTransactionId(transaction.getId());
                movement.setReference(transaction.getReference());
                movement.setType(transaction.getType());
                movement.setDescription(line.getDescription() != null ? line.getDescription() : transaction.getDescription());
                movement.setProductId(product.getId());
                movement.setProductCode(product.getCode());
                movement.setProductName(product.getName());
                movement.setQuantityIn(quantityIn);
                movement.setQuantityOut(quantityOut);
                movement.setRunningBalance(balance);
                movement.setUnit(product.getUnit());

                movements.add(movement);
            }
        }

        return movements;
    }

    // DTO for Movement History data
    private static class MovementHistoryDTO {
        private LocalDate date;
        private Long transactionId;
        private String reference;
        private InventoryTransaction.TransactionType type;
        private String description;
        private Long productId;
        private String productCode;
        private String productName;
        private int quantityIn;
        private int quantityOut;
        private int runningBalance;
        private String unit;

        // Getters and setters
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(Long transactionId) {
            this.transactionId = transactionId;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public InventoryTransaction.TransactionType getType() {
            return type;
        }

        public void setType(InventoryTransaction.TransactionType type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantityIn() {
            return quantityIn;
        }

        public void setQuantityIn(int quantityIn) {
            this.quantityIn = quantityIn;
        }

        public int getQuantityOut() {
            return quantityOut;
        }

        public void setQuantityOut(int quantityOut) {
            this.quantityOut = quantityOut;
        }

        public int getRunningBalance() {
            return runningBalance;
        }

        public void setRunningBalance(int runningBalance) {
            this.runningBalance = runningBalance;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}