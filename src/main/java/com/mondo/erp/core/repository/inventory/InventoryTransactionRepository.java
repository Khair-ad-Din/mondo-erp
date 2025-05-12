// src/main/java/com/mondo/erp/core/repository/inventory/InventoryTransactionRepository.java
package com.mondo.erp.core.repository.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.inventory.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByCompany(Company company);

    List<InventoryTransaction> findByCompanyAndType(Company company, InventoryTransaction.TransactionType type);

    List<InventoryTransaction> findByCompanyAndTransactionDateBetween(Company company, LocalDate startDate, LocalDate endDate);

    Optional<InventoryTransaction> findByReferenceAndCompany(String reference, Company company);
}