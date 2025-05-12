// src/main/java/com/mondo/erp/core/repository/inventory/ProductRepository.java
package com.mondo.erp.core.repository.inventory;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.inventory.Product;
import com.mondo.erp.core.model.inventory.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCompany(Company company);

    List<Product> findByCompanyAndActive(Company company, boolean active);

    List<Product> findByCategory(ProductCategory category);

    List<Product> findByCategoryAndCompany(ProductCategory category, Company company);

    Optional<Product> findByCodeAndCompany(String code, Company company);

    boolean existsByCodeAndCompany(String code, Company company);
}