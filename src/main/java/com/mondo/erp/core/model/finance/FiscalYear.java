package com.mondo.erp.core.model.finance;


import com.mondo.erp.core.model.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_years")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiscalYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FiscalYearStatus status = FiscalYearStatus.OPEN;

    @Column(nullable = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum FiscalYearStatus {
        OPEN, CLOSED
    }

    //Helper Methods
    public boolean isDateFiscalYear(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

}
