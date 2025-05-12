// src/main/java/com/mondo/erp/core/service/inventory/impl/ProductServiceImpl.java
package com.mondo.erp.core.service.inventory.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.inventory.Product;
import com.mondo.erp.core.model.inventory.ProductCategory;
import com.mondo.erp.core.repository.inventory.ProductRepository;
import com.mondo.erp.core.service.inventory.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findByCompany(Company company) {
        return productRepository.findByCompany(company);
    }

    @Override
    public List<Product> findByCompanyAndActive(Company company, boolean active) {
        return productRepository.findByCompanyAndActive(company, active);
    }

    @Override
    public List<Product> findByCategory(ProductCategory category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public List<Product> findByCategoryAndCompany(ProductCategory category, Company company) {
        return productRepository.findByCategoryAndCompany(category, company);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> findByCodeAndCompany(String code, Company company) {
        return productRepository.findByCodeAndCompany(code, company);
    }

    @Override
    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public boolean existsByCodeAndCompany(String code, Company company) {
        return productRepository.existsByCodeAndCompany(code, company);
    }

    @Override
    public boolean isCodeUnique(String code, Company company, Long excludeId) {
        Optional<Product> existingProduct = productRepository.findByCodeAndCompany(code, company);
        return !existingProduct.isPresent() || existingProduct.get().getId().equals(excludeId);
    }
}