package com.mondo.erp.core.repository;

import com.mondo.erp.core.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByTaxId(String taxId);

    boolean existsByTaxId(String taxId);
}
