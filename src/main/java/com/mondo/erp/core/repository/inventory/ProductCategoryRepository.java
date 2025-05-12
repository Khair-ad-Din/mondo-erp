// src/main/java/com/mondo/erp/core/repository/inventory/ProductCategoryRepository.java
package com.mondo.erp.core.repository.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.inventory.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByCompany(Company company);

    Optional<ProductCategory> findByNameAndCompany(String name, Company company);

    boolean existsByNameAndCompany(String name, Company company);
}