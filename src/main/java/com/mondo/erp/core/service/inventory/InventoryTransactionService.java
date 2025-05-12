// src/main/java/com/mondo/erp/core/service/inventory/InventoryTransactionService.java
package com.mondo.erp.core.service.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.inventory.InventoryTransaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryTransactionService {

    List<InventoryTransaction> findByCompany(Company company);

    List<InventoryTransaction> findByCompanyAndType(Company company, InventoryTransaction.TransactionType type);

    List<InventoryTransaction> findByCompanyAndDateRange(Company company, LocalDate startDate, LocalDate endDate);

    Optional<InventoryTransaction> findById(Long id);

    Optional<InventoryTransaction> findByReferenceAndCompany(String reference, Company company);

    InventoryTransaction save(InventoryTransaction transaction);

    // Special methods for different transaction types that integrate with accounting
    InventoryTransaction createPurchase(InventoryTransaction purchase, String accountingMethod, User user);

    InventoryTransaction createSale(InventoryTransaction sale, String accountingMethod, User user);

    InventoryTransaction createAdjustment(InventoryTransaction adjustment, User user);

    String generateNextReference(Company company, InventoryTransaction.TransactionType type);

    boolean isReferenceUnique(String reference, Company company, Long excludeId);
}