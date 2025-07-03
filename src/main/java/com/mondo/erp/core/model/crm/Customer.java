package com.mondo.erp.core.model.crm;

import com.mondo.erp.core.model.Company;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 20, message = "Tax ID cannot exceed 20 characters")
    @Column(name = "tax_id", length = 20)  // Sin unique aquí
    private String taxId;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(length = 100)
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    @Column(length = 20)
    private String phone;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    @Column(length = 255)
    private String address;

    @Size(max = 255, message = "Payment terms cannot exceed 255 characters")
    @Column(name = "payment_terms", length = 255)  // Mapeo correcto
    private String paymentTerms;  // camelCase

    @DecimalMin(value = "0.0", message = "Credit limit cannot be negative")
    @Column(name = "credit_limit", precision = 19, scale = 4)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;  // active, no isActive

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY para rendimiento
    @JoinColumn(name = "company_id", nullable = false)
    @NotNull(message = "Company is required")
    private Company company;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor por defecto
    public Customer() {}

    // Constructor con campos principales
    public Customer(String name, Company company) {
        this.name = name;
        this.company = company;
    }

    // Métodos de conveniencia para relaciones
    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setCustomer(this);
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setCustomer(null);
    }

    // Getters y Setters (SIN Lombok para mantener consistencia)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentTerms() {  // Método corregido
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {  // Método corregido
        this.paymentTerms = paymentTerms;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals y hashCode (importantes para JPA)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return id != null && id.equals(customer.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", taxId='" + taxId + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}