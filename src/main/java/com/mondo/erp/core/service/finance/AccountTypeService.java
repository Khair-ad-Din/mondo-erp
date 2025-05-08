package com.mondo.erp.core.service.finance;

import com.mondo.erp.core.model.finance.AccountType;
import java.util.List;
import java.util.Optional;

public interface AccountTypeService {

    List<AccountType> findAll();

    List<AccountType> findByCategory(AccountType.AccountCategory category);

    Optional<AccountType> findById(Long id);

    Optional<AccountType> findByCode(String code);

    AccountType save(AccountType accountType);

    void deleteById(Long id);

    boolean isCodeUnique(String code, Long excludeId);
}
