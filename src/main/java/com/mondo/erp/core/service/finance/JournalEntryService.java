// src/main/java/com/mondo/erp/core/service/finance/JournalEntryService.java
package com.mondo.erp.core.service.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.finance.JournalEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalEntryService {

    List<JournalEntry> findByCompany(Company company);

    List<JournalEntry> findByCompanyAndEntryDateBetween(Company company, LocalDate startDate, LocalDate endDate);

    List<JournalEntry> findByStatusAndCompany(JournalEntry.JournalEntryStatus status, Company company);

    Optional<JournalEntry> findById(Long id);

    Optional<JournalEntry> findByReferenceAndCompany(String reference, Company company);

    JournalEntry save(JournalEntry journalEntry);

    void deleteById(Long id);

    JournalEntry postJournalEntry(JournalEntry journalEntry, User user);

    JournalEntry approveJournalEntry(JournalEntry journalEntry, User user);

    String generateNextReference(Company company);

    boolean isReferenceUnique(String reference, Company company, Long excludeId);

    long countAll();
}