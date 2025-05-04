package com.mondo.erp.core.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String cif;

    private String direction;

    private String town;

    private String county;

    @Column(name = "postal_code")
    private String postalCode;

    private String telephoneNumber;

    private String email;

    @Column(name = "web_site")
    private String webSite;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creationTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;





}
