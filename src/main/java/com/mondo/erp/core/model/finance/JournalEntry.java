package com.mondo.erp.core.model.finance;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private LocalDate entryDate;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalEntryStatus status = JournalEntryStatus.DRAFT;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalEntryLine> entryLines = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "fiscal_year_id")
    private FiscalYear fiscalYear;

    @ManyToOne
    @JoinColumn(name="created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public BigDecimal getDebitTotal() {
        return entryLines.stream()
                .map(JournalEntryLine::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getCreditTotal() {
        return entryLines.stream()
                .map(JournalEntryLine::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isBalanced() {
        return getDebitTotal().compareTo(getCreditTotal()) == 0;
    }

    public enum JournalEntryStatus {
        DRAFT, POSTED, APPROVED, VOIDED
    }
}
