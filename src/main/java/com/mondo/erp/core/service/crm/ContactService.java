package com.mondo.erp.core.service.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Contact;
import com.mondo.erp.core.model.crm.Customer;
import com.mondo.erp.core.model.crm.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ContactService {

    // ========================
    // OPERACIONES CRUD BÁSICAS
    // ========================

    Contact save(Contact contact);

    Optional<Contact> findById(Long id);

    void deleteById(Long id);

    void delete(Contact contact);

    // ========================
    // CONSULTAS POR CUSTOMER
    // ========================

    List<Contact> findByCustomer(Customer customer);
    List<Contact> findByCustomerAndContactType(Customer customer, Contact.ContactType contactType);
    Page<Contact> findByCustomer(Customer customer, Pageable pageable);

    // ========================
    // CONSULTAS POR SUPPLIER
    // ========================

    List<Contact> findBySupplier(Supplier supplier);
    List<Contact> findBySupplierAndContactType(Supplier supplier, Contact.ContactType contactType);
    Page<Contact> findBySupplier(Supplier supplier, Pageable pageable);

    // ========================
    // CONSULTAS POR COMPANY
    // ========================

    List<Contact> findByCompany(Company company);

    Page<Contact> findByCompany(Company company, Pageable pageable);

    List<Contact> findCustomerContactsByCompany(Company company);
    List<Contact> findSupplierContactsByCompany(Company company);

    Page<Contact> findWithFilters(Company company, Contact.ContactType contactType,
                                  Boolean isCusterContact, String searchTerm, Pageable pageable);

    // ========================
    // OPERACIONES ESPECÍFICAS DE CONTACTO
    // ========================

    //Crea un contacto para un cliente y para un proveedor
    Contact createCustomerContact(Customer customer, String firstName, String lastName,
                                  Contact.ContactType contactType);
    Contact createSupplierContact(Supplier supplier, String firstName, String lastName,
                                  Contact.ContactType contactType);

    // Transfiere un contacto de customer a supplier y viceversa
    Contact transferContactToSupplier(Long contactId, Supplier supplier);
    Contact transferContactToCustomer(Long contactId, Customer customer);

    // ========================
    // VALIDACIONES DE NEGOCIO
    // ========================

    boolean isEmailUnique (String email, Company company, Long excludeId);

    void validateContact(Contact contact);

    // Valida que el contacto tenga exactamente un parent (customer o supplier)
    void validateExclusiveRelation(Contact contact);

    // ========================
    // ESTADÍSTICAS
    // ========================

    long countByCompany(Company company);

    long countByCustomer(Customer customer);

    long countBySupplier(Supplier supplier);

    // Obtiene estadísticas de contactos por tipo
    List<Object[]> getContactCountByType(Company company);

    // Encuentra contactos principales (tipo COMMERCIAL) por empresa
    List<Contact> findPrimaryContactsByCompany(Company company);


}
