package com.mondo.erp.core.model.finance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountCategory category;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "accountType", cascade = CascadeType.ALL)
    private List<Account> accounts = new ArrayList<>();

    public enum AccountCategory {
        ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    }

    //----------


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
