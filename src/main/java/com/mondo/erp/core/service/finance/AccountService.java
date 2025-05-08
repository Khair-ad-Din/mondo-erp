package com.mondo.erp.core.service.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.Account;
import com.mondo.erp.core.model.finance.AccountType;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    List<Account> findAllByCompany(Company company);

    List<Account> findActiveAccountsByCompany(Company company);

    List<Account> findRootAccounts(Company company);

    Optional<Account> findById(Long id);

    Optional<Account> findByCode(String code, Company company);

    Account save(Account account);

    void deleteById(Long id);

    List<Account> findByAccountType(AccountType accountType, Company company);

    boolean isCodeUnique(String code, Company company, Long excludeId);
}
