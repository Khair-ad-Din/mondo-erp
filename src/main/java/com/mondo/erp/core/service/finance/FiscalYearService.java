// src/main/java/com/mondo/erp/core/service/finance/FiscalYearService.java
package com.mondo.erp.core.service.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.FiscalYear;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FiscalYearService {

    List<FiscalYear> findAllByCompany(Company company);

    List<FiscalYear> findOpenFiscalYears(Company company);

    Optional<FiscalYear> findByDate(LocalDate date, Company company);

    Optional<FiscalYear> findById(Long id);

    Optional<FiscalYear> findByNameAndCompany(String name, Company company);

    FiscalYear save(FiscalYear fiscalYear);

    void deleteById(Long id);

    boolean isNameUnique(String name, Company company, Long excludeId);
}