package com.mondo.erp.core.repository.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Contact;
import com.mondo.erp.core.model.crm.Customer;
import com.mondo.erp.core.model.crm.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // ========================
    // BÚSQUEDAS POR CUSTOMER
    // ========================

    // Encuentra todos los contactos de un cliente
    List<Contact> findByCustomer(Customer customer);

    // Encuentra contactos de un cliente por tipo
    List<Contact> findByCustomerAndContactType(Customer customer, Contact.ContactType contactType);

    // Encuentra contactos de un cliente con paginación
    Page<Contact> findByCustomer(Customer customer, Pageable pageable);

    // ========================
    // BÚSQUEDAS POR SUPPLIER
    // ========================

    List<Contact> findBySupplier(Supplier supplier);

    List<Contact> findBySupplierAndContactType(Supplier supplier, Contact.ContactType contactType);

    Page<Contact> findBySupplier(Supplier supplier, Pageable pageable);

    // ========================
    // BÚSQUEDAS POR COMPANY (a través de customer/supplier)
    // ========================

    // Encuentra todos los contactos de una empresa (customers + suppliers)
    @Query("SELECT c FROM Contact c WHERE " +
            "(c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            "(c.supplier IS NOT NULL AND c.supplier.company = :company)")
    List<Contact> findByCompany(@Param("company") Company company);


    // Encuentra contactos de una empresa con paginación
    @Query("SELECT c FROM Contact c WHERE " +
            "(c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            "(c.supplier IS NOT NULL AND c.supplier.company = :company)")
    Page<Contact> findByCompany(@Param("company") Company company, Pageable pageable);

    // Encuentra contactos de customers de una empresa
    @Query("SELECT c FROM Contact c WHERE c.customer IS NOT NULL " +
            "AND c.customer.company = :company")
    List<Contact> findCustomerContactsByCompany(@Param("company") Company company);

    // Encuentra contactos de suppliers de una empresa
    @Query("SELECT c FROM Contact c WHERE c.supplier IS NOT NULL " +
            "AND c.supplier.company = :company")
    List<Contact> findSupplierContactsByCompany(@Param("company") Company company);

    // ========================
    // BÚSQUEDAS AVANZADAS
    // ========================

    // Búsqueda de texto en contactos de una empresa
    @Query("SELECT c FROM Contact c WHERE " +
            "((c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            " (c.supplier IS NOT NULL AND c.supplier.company = :company)) AND " +
            "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(c.position) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Contact> findByCompanyAndSearchTerm(@Param("company") Company company,
                                             @Param("searchTerm") String searchTerm,
                                             Pageable pageable);

    // Búsqueda con filtros múltiples
    @Query("SELECT c FROM Contact c WHERE " +
            "((c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            " (c.supplier IS NOT NULL AND c.supplier.company = :company)) AND " +
            "(:contactType IS NULL OR c.contactType = :contactType) AND " +
            "(:isCustomerContact IS NULL OR " +
            " (:isCustomerContact = true AND c.customer IS NOT NULL) OR " +
            " (:isCustomerContact = false AND c.supplier IS NOT NULL)) AND " +
            "(:searchTerm IS NULL OR " +
            " LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Contact> findByCompanyWithFilters(@Param("company") Company company,
                                           @Param("contactType") Contact.ContactType contactType,
                                           @Param("isCustomerContact") Boolean isCustomerContact,
                                           @Param("searchTerm") String searchTerm,
                                           Pageable pageable);

    // ========================
    // VALIDACIONES
    // ========================

    // Verifica si existe un email en los contactos de una empresa
    @Query("SELECT COUNT(c) > 0 FROM Contact c WHERE c.email = :email AND " +
            "((c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            " (c.supplier IS NOT NULL AND c.supplier.company = :company))")
    boolean existsByEmailAndCompany(@Param("email") String email, @Param("company") Company company);

    // Verifica unicidad de email excluyendo un contacto específico
    @Query("SELECT COUNT(c) > 0 FROM Contact c WHERE c.email = :email AND c.id != :excludeId AND " +
            "((c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            " (c.supplier IS NOT NULL AND c.supplier.company = :company))")
    boolean existsByEmailAndCompanyExcludingId(@Param("email") String email,
                                               @Param("company") Company company,
                                               @Param("excludeId") Long excludeId);

    // ========================
    // ESTADÍSTICAS
    // ========================

    // Cuenta contactos por tipo en una empresa
    @Query("SELECT c.contactType, COUNT(c) FROM Contact c WHERE " +
            "((c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            " (c.supplier IS NOT NULL AND c.supplier.company = :company)) " +
            "GROUP BY c.contactType")
    List<Object[]> countByTypeAndCompany(@Param("company") Company company);

    // Cuenta total de contactos de una empresa
    @Query("SELECT COUNT(c) FROM Contact c WHERE " +
            "(c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            "(c.supplier IS NOT NULL AND c.supplier.company = :company)")
    long countByCompany(@Param("company") Company company);

    // ========================
    // PROYECCIONES
    // ========================

    interface ContactBasicInfo {
        Long getId();
        String getFirstName();
        String getLastName();
        String getEmail();
        String getPosition();
        Contact.ContactType getContactType();

        // Para saber si es customer o supplier contact
        default String getParentType() {
            return getCustomerId() != null ? "Customer" : "Supplier";
        }

        Long getCustomerId();
        Long getSupplierId();
        String getCustomerName();
        String getSupplierName();
    }

    @Query("SELECT c.id AS id, c.firstName AS firstName, c.lastName AS lastName, " +
            "c.email AS email, c.position AS position, c.contactType AS contactType, " +
            "c.customer.id AS customerId, c.supplier.id AS supplierId, " +
            "c.customer.name AS customerName, c.supplier.name AS supplierName " +
            "FROM Contact c WHERE " +
            "(c.customer IS NOT NULL AND c.customer.company = :company) OR " +
            "(c.supplier IS NOT NULL AND c.supplier.company = :company)")
    List<ContactBasicInfo> findBasicInfoByCompany(@Param("company") Company company);
}
