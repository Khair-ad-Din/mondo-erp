package com.mondo.erp.core.service;

import com.mondo.erp.core.model.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyService {

    List<Company> findAll();

    Optional<Company> findById(Long id);

    Optional<Company> findByTaxId(String taxId);

    Company save(Company company);

    void deleteById(Long id);

    boolean existsByTaxId(String taxId);

    long countAll();
}
