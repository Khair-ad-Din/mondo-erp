// src/main/java/com/mondo/erp/core/service/finance/impl/FiscalYearServiceImpl.java (continued)
package com.mondo.erp.core.service.finance.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.FiscalYear;
import com.mondo.erp.core.repository.finance.FiscalYearRepository;
import com.mondo.erp.core.service.finance.FiscalYearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FiscalYearServiceImpl implements FiscalYearService {

    private final FiscalYearRepository fiscalYearRepository;

    @Autowired
    public FiscalYearServiceImpl(FiscalYearRepository fiscalYearRepository) {
        this.fiscalYearRepository = fiscalYearRepository;
    }

    @Override
    public List<FiscalYear> findAllByCompany(Company company) {
        return fiscalYearRepository.findByCompanyOrderByStartDateDesc(company);
    }

    @Override
    public List<FiscalYear> findOpenFiscalYears(Company company) {
        return fiscalYearRepository.findByCompanyOrderByStartDateDesc(company).stream()
                .filter(year -> year.getStatus() == FiscalYear.FiscalYearStatus.OPEN)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FiscalYear> findByDate(LocalDate date, Company company) {
        return fiscalYearRepository.findFiscalYearByDate(date, company);
    }

    @Override
    public Optional<FiscalYear> findById(Long id) {
        return fiscalYearRepository.findById(id);
    }

    @Override
    public Optional<FiscalYear> findByNameAndCompany(String name, Company company) {
        return fiscalYearRepository.findByNameAndCompany(name, company);
    }

    @Override
    public FiscalYear save(FiscalYear fiscalYear) {
        return fiscalYearRepository.save(fiscalYear);
    }

    @Override
    public void deleteById(Long id) {
        fiscalYearRepository.deleteById(id);
    }

    @Override
    public boolean isNameUnique(String name, Company company, Long excludeId) {
        Optional<FiscalYear> existingYear = fiscalYearRepository.findByNameAndCompany(name, company);
        return !existingYear.isPresent() || existingYear.get().getId().equals(excludeId);
    }
}