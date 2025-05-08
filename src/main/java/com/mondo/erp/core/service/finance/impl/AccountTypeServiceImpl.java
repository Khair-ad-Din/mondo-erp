package com.mondo.erp.core.service.finance.impl;

import com.mondo.erp.core.model.finance.AccountType;
import com.mondo.erp.core.repository.finance.AccountTypeRepository;
import com.mondo.erp.core.service.finance.AccountTypeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class AccountTypeServiceImpl implements AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;

    @Autowired
    public AccountTypeServiceImpl(AccountTypeRepository accountTypeRepository) {
        this.accountTypeRepository = accountTypeRepository;
    }

    @Override
    public List<AccountType> findAll() {
        return accountTypeRepository.findAll();
    }

    @Override
    public List<AccountType> findByCategory(AccountType.AccountCategory category) {
        return accountTypeRepository.findByCategory(category);
    }

    @Override
    public Optional<AccountType> findById(Long id) {
        return accountTypeRepository.findById(id);
    }

    @Override
    public Optional<AccountType> findByCode(String code) {
        return accountTypeRepository.findByCode(code);
    }

    @Override
    public AccountType save(AccountType accountType) {
        return accountTypeRepository.save(accountType);
    }

    @Override
    public void deleteById(Long id) {
        accountTypeRepository.deleteById(id);
    }

    @Override
    public boolean isCodeUnique(String code, Long excludeId) {
        Optional<AccountType> existingType = accountTypeRepository.findByCode(code);
        return !existingType.isPresent() || existingType.get().getId().equals(excludeId);
    }
}
