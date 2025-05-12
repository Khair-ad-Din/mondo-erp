// src/main/java/com/mondo/erp/core/service/inventory/ProductCategoryService.java
package com.mondo.erp.core.service.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.inventory.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryService {

    List<ProductCategory> findByCompany(Company company);

    Optional<ProductCategory> findById(Long id);

    Optional<ProductCategory> findByNameAndCompany(String name, Company company);

    ProductCategory save(ProductCategory category);

    void deleteById(Long id);

    boolean existsByNameAndCompany(String name, Company company);

    boolean isNameUnique(String name, Company company, Long excludeId);
}