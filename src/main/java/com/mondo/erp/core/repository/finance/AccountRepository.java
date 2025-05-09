package com.mondo.erp.core.repository.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByCompany(Company company);

    List<Account> findByCompanyAndActiveTrue (Company company);

    Optional<Account> findByCodeAndCompany(String code, Company company);

    @Query("SELECT a FROM Account a WHERE a.parent IS NULL AND a.company = ?1")
    List<Account> findRootAccounts(Company company);
}
