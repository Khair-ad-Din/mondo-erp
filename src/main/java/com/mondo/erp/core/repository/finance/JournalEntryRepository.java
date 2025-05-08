package com.mondo.erp.core.repository.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.finance.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    Optional<JournalEntry> findByReferenceAndCompany (String reference, Company company);

    List<JournalEntry> findByCompanyAndEntryDateBetween(Company company, LocalDate startDate, LocalDate endDate);

    Page<JournalEntry> findByCompanyOrderByEntryDateDesc(Company company, Pageable pageable);

    List<JournalEntry> findByStatusAndCompany(JournalEntry.JournalEntryStatus status, Company company);
}
