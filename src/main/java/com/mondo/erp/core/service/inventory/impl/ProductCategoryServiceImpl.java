// src/main/java/com/mondo/erp/core/service/inventory/impl/ProductCategoryServiceImpl.java
package com.mondo.erp.core.service.inventory.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.inventory.ProductCategory;
import com.mondo.erp.core.repository.inventory.ProductCategoryRepository;
import com.mondo.erp.core.service.inventory.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Autowired
    public ProductCategoryServiceImpl(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<ProductCategory> findByCompany(Company company) {
        return categoryRepository.findByCompany(company);
    }

    @Override
    public Optional<ProductCategory> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<ProductCategory> findByNameAndCompany(String name, Company company) {
        return categoryRepository.findByNameAndCompany(name, company);
    }

    @Override
    @Transactional
    public ProductCategory save(ProductCategory category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public boolean existsByNameAndCompany(String name, Company company) {
        return categoryRepository.existsByNameAndCompany(name, company);
    }

    @Override
    public boolean isNameUnique(String name, Company company, Long excludeId) {
        Optional<ProductCategory> existingCategory = categoryRepository.findByNameAndCompany(name, company);
        return !existingCategory.isPresent() || existingCategory.get().getId().equals(excludeId);
    }
}