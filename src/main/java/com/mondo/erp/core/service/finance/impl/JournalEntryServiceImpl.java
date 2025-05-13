// src/main/java/com/mondo/erp/core/service/finance/impl/JournalEntryServiceImpl.java
package com.mondo.erp.core.service.finance.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.finance.JournalEntry;
import com.mondo.erp.core.repository.finance.JournalEntryRepository;
import com.mondo.erp.core.service.finance.JournalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryServiceImpl implements JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    @Autowired
    public JournalEntryServiceImpl(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public List<JournalEntry> findByCompany(Company company) {
        return journalEntryRepository.findByCompanyOrderByEntryDateDesc(company, Pageable.unpaged()).getContent();
    }

    @Override
    public List<JournalEntry> findByCompanyAndEntryDateBetween(Company company, LocalDate startDate, LocalDate endDate) {
        return journalEntryRepository.findByCompanyAndEntryDateBetween(company, startDate, endDate);
    }

    @Override
    public List<JournalEntry> findByStatusAndCompany(JournalEntry.JournalEntryStatus status, Company company) {
        return journalEntryRepository.findByStatusAndCompany(status, company);
    }

    @Override
    public Optional<JournalEntry> findById(Long id) {
        return journalEntryRepository.findById(id);
    }

    @Override
    public Optional<JournalEntry> findByReferenceAndCompany(String reference, Company company) {
        return journalEntryRepository.findByReferenceAndCompany(reference, company);
    }

    @Override
    @Transactional
    public JournalEntry save(JournalEntry journalEntry) {
        // Set line numbers if not already set
        for (int i = 0; i < journalEntry.getEntryLines().size(); i++) {
            if (journalEntry.getEntryLines().get(i).getLineNumber() == null) {
                journalEntry.getEntryLines().get(i).setLineNumber(i + 1);
            }
            // Set the journal entry reference in each line
            journalEntry.getEntryLines().get(i).setJournalEntry(journalEntry);
        }

        return journalEntryRepository.save(journalEntry);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        journalEntryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public JournalEntry postJournalEntry(JournalEntry journalEntry, User user) {
        if (journalEntry.getStatus() != JournalEntry.JournalEntryStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT journal entries can be posted");
        }

        if (!journalEntry.isBalanced()) {
            throw new IllegalStateException("Journal entry must be balanced before posting");
        }

        journalEntry.setStatus(JournalEntry.JournalEntryStatus.POSTED);
        journalEntry.setCreatedBy(user);

        return journalEntryRepository.save(journalEntry);
    }

    @Override
    @Transactional
    public JournalEntry approveJournalEntry(JournalEntry journalEntry, User user) {
        if (journalEntry.getStatus() != JournalEntry.JournalEntryStatus.POSTED) {
            throw new IllegalStateException("Only POSTED journal entries can be approved");
        }

        journalEntry.setStatus(JournalEntry.JournalEntryStatus.APPROVED);
        journalEntry.setApprovedBy(user);

        return journalEntryRepository.save(journalEntry);
    }

    @Override
    public String generateNextReference(Company company) {
        int currentYear = Year.now().getValue();

        // Find the highest reference number for this year
        String prefix = "JE-" + currentYear + "-";

        List<JournalEntry> entries = journalEntryRepository.findByCompanyOrderByEntryDateDesc(company, Pageable.unpaged()).getContent();

        int maxNumber = 0;
        for (JournalEntry entry : entries) {
            if (entry.getReference().startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(entry.getReference().substring(prefix.length()));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore if format is different
                }
            }
        }

        // Format with leading zeros
        return prefix + String.format("%03d", maxNumber + 1);
    }

    @Override
    public boolean isReferenceUnique(String reference, Company company, Long excludeId) {
        Optional<JournalEntry> existingEntry = journalEntryRepository.findByReferenceAndCompany(reference, company);
        return !existingEntry.isPresent() || existingEntry.get().getId().equals(excludeId);
    }

    @Override
    public long countAll() {
        return journalEntryRepository.count();
    }
}