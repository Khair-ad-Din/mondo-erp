package com.mondo.erp.core.model.crm;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters") // ✅ Corregido el mensaje
    @Column(length = 100)
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters") // ✅ Corregido el mensaje
    @Column(length = 20)
    private String phone;

    @Size(max = 100, message = "Position cannot exceed 100 characters")
    @Column(length = 100)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false, length = 20)
    @NotNull(message = "Contact Type is required")
    // ✅ ELIMINADO @Size ya que no puede aplicarse a enums
    private ContactType contactType;

    // Relaciones opcionales y mutuamente exclusivas
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ContactType {
        COMMERCIAL("Commercial"),
        TECHNICAL("Technical"),
        ADMIN("Administrative");

        private final String displayName;

        ContactType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Contact() {
    }

    // Constructor para contacto de Customer
    public Contact(String firstName, String lastName, ContactType contactType, Customer customer) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactType = contactType;
        this.customer = customer;
    }

    // Constructor para contacto de Supplier
    public Contact(String firstName, String lastName, ContactType contactType, Supplier supplier) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactType = contactType;
        this.supplier = supplier;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isCustomerContact() {
        return customer != null;
    }

    public boolean isSupplierContact() {
        return supplier != null;
    }

    // Validación personalizada para asegurar que solo un parent esté asignado
    @PrePersist
    @PreUpdate
    private void validateExclusiveRelation() {
        if ((customer != null && supplier != null) || (customer == null && supplier == null)) {
            throw new IllegalStateException("Contact must belong exactly to one Customer OR one Supplier");
        }
    }

    // ========================
    // GETTERS Y SETTERS
    // ========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public void setContactType(ContactType contactType) {
        this.contactType = contactType;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            this.supplier = null; // Asegura exclusividad
        }
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        if (supplier != null) {
            this.customer = null; // Asegura exclusividad
        }
    }

    // ========================
    // EQUALS, HASHCODE Y TOSTRING
    // ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return id != null && id.equals(contact.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", contactType=" + contactType +
                ", isCustomerContact=" + isCustomerContact() +
                '}';
    }
}