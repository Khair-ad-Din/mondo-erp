package com.mondo.erp.core.service.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.repository.CompanyRepository;
import com.mondo.erp.core.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }


    @Override
    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    @Override
    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    @Override
    public Optional<Company> findByTaxId(String taxId) {
        return companyRepository.findByTaxId(taxId);
    }

    @Override
    public Company save(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public void deleteById(Long id) {
        companyRepository.deleteById(id);
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return companyRepository.existsByTaxId(taxId);
    }

    @Override
    public long countAll() {
        return companyRepository.count();
    }
}
