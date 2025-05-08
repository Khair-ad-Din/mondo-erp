package com.mondo.erp.core.repository.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.FiscalYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FiscalYearRepository extends JpaRepository<FiscalYear, Long> {

    List<FiscalYear> findByCompanyOrderByStartDateDesc (Company company);

    Optional<FiscalYear> findByNameAndCompany(String name, Company company);

    @Query("SELECT fy FROM FiscalYear fy WHERE ?1 BETWEEN fy.startDate AND fy.endDate AND fy.company = ?2")
    Optional<FiscalYear> findFiscalYearByDate(LocalDate date, Company company);

    Optional<FiscalYear> findFirstByCompanyOrderByEndDateDesc(Company company);
}
