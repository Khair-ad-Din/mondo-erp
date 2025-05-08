package com.mondo.erp.core.repository.finance;

import com.mondo.erp.core.model.finance.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {

    Optional<AccountType> findByCode(String code);

    List<AccountType> findByCategory (AccountType.AccountCategory category);
}
