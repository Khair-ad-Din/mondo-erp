// src/main/java/com/mondo/erp/core/service/inventory/ProductService.java
package com.mondo.erp.core.service.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.inventory.Product;
import com.mondo.erp.core.model.inventory.ProductCategory;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findByCompany(Company company);

    List<Product> findByCompanyAndActive(Company company, boolean active);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByCategoryAndCompany(ProductCategory category, Company company);

    Optional<Product> findById(Long id);

    Optional<Product> findByCodeAndCompany(String code, Company company);

    Product save(Product product);

    void deleteById(Long id);

    boolean existsByCodeAndCompany(String code, Company company);

    boolean isCodeUnique(String code, Company company, Long excludeId);
}