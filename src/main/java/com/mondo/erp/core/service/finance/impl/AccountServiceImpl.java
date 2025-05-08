package com.mondo.erp.core.service.finance.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.Account;
import com.mondo.erp.core.model.finance.AccountType;
import com.mondo.erp.core.repository.finance.AccountRepository;
import com.mondo.erp.core.service.finance.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Account> findAllByCompany(Company company) {
        return accountRepository.findByCompany(company);
    }

    @Override
    public List<Account> findActiveAccountsByCompany(Company company) {
        return accountRepository.findByCompanyAndActiveTrue(company);
    }

    @Override
    public List<Account> findRootAccounts(Company company) {
        return accountRepository.findRootAccounts(company);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> findByCode(String code, Company company) {
        return accountRepository.findByCodeAndCompany(code, company);
    }

    @Override
    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public List<Account> findByAccountType(AccountType accountType, Company company) {
        return accountRepository.findByCompany(company).stream()
                .filter(account -> account.getAccountType().equals(accountType))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isCodeUnique(String code, Company company, Long excludeId) {
        Optional<Account> existingAccount = accountRepository.findByCodeAndCompany(code, company);
        return !existingAccount.isPresent() || existingAccount.get().getId().equals(excludeId);
    }
}
