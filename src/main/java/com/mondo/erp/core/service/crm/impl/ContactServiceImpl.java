package com.mondo.erp.core.service.crm.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Contact;
import com.mondo.erp.core.model.crm.Customer;
import com.mondo.erp.core.model.crm.Supplier;
import com.mondo.erp.core.repository.crm.ContactRepository;
import com.mondo.erp.core.service.crm.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;

    @Autowired
    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    // ========================
    // OPERACIONES CRUD BÁSICAS
    // ========================

    @Override
    public Contact save(Contact contact) {
        logger.debug("Saving contact: {} {}", contact.getFirstName(), contact.getLastName());

        // Validar antes de guardar
        validateContact(contact);

        // Guardar y retornar
        Contact savedContact = contactRepository.save(contact);
        logger.info("Contact saved successfully with ID: {} ({})",
                savedContact.getId(), savedContact.getFullName());

        return savedContact;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contact> findById(Long id) {
        logger.debug("Finding contact by ID: {}", id);
        return contactRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Deleting contact with ID: {}", id);

        Optional<Contact> contactOpt = contactRepository.findById(id);
        if (contactOpt.isPresent()) {
            Contact contact = contactOpt.get();

            // Log información antes de eliminar
            if (contact.isCustomerContact()) {
                logger.info("Deleting contact {} from customer: {}",
                        contact.getFullName(), contact.getCustomer().getName());
            } else {
                logger.info("Deleting contact {} from supplier: {}",
                        contact.getFullName(), contact.getSupplier().getName());
            }
        }

        contactRepository.deleteById(id);
        logger.info("Contact with ID {} deleted successfully", id);
    }

    @Override
    public void delete(Contact contact) {
        logger.debug("Deleting contact: {}", contact.getFullName());

        if (contact.isCustomerContact()) {
            logger.info("Deleting contact {} from customer: {}",
                    contact.getFullName(), contact.getCustomer().getName());
        } else {
            logger.info("Deleting contact {} from supplier: {}",
                    contact.getFullName(), contact.getSupplier().getName());
        }

        contactRepository.delete(contact);
        logger.info("Contact {} deleted successfully", contact.getFullName());
    }

    // ========================
    // CONSULTAS POR CUSTOMER
    // ========================

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findByCustomer(Customer customer) {
        logger.debug("Finding contacts for customer: {}", customer.getName());
        return contactRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findByCustomerAndContactType(Customer customer, Contact.ContactType contactType) {
        logger.debug("Finding {} contacts for customer: {}", contactType, customer.getName());
        return contactRepository.findByCustomerAndContactType(customer, contactType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> findByCustomer(Customer customer, Pageable pageable) {
        logger.debug("Finding contacts for customer with pagination: {}", customer.getName());
        return contactRepository.findByCustomer(customer, pageable);
    }

    // ========================
    // CONSULTAS POR SUPPLIER
    // ========================

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findBySupplier(Supplier supplier) {
        logger.debug("Finding contacts for supplier: {}", supplier.getName());
        return contactRepository.findBySupplier(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findBySupplierAndContactType(Supplier supplier, Contact.ContactType contactType) {
        logger.debug("Finding {} contacts for supplier: {}", contactType, supplier.getName());
        return contactRepository.findBySupplierAndContactType(supplier, contactType);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> findBySupplier(Supplier supplier, Pageable pageable) {
        logger.debug("Finding contacts for supplier with pagination: {}", supplier.getName());
        return contactRepository.findBySupplier(supplier, pageable);
    }

    // ========================
    // CONSULTAS POR COMPANY
    // ========================

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findByCompany(Company company) {
        logger.debug("Finding all contacts for company: {}", company.getName());
        return contactRepository.findByCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> findByCompany(Company company, Pageable pageable) {
        logger.debug("Finding contacts for company with pagination: {}", company.getName());
        return contactRepository.findByCompany(company, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findCustomerContactsByCompany(Company company) {
        logger.debug("Finding customer contacts for company: {}", company.getName());
        return contactRepository.findCustomerContactsByCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findSupplierContactsByCompany(Company company) {
        logger.debug("Finding supplier contacts for company: {}", company.getName());
        return contactRepository.findSupplierContactsByCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> findWithFilters(Company company, Contact.ContactType contactType,
                                         Boolean isCustomerContact, String searchTerm, Pageable pageable) {
        logger.debug("Finding contacts with filters - Company: {}, Type: {}, IsCustomer: {}, Search: {}",
                company.getName(), contactType, isCustomerContact, searchTerm);
        return contactRepository.findByCompanyWithFilters(company, contactType, isCustomerContact, searchTerm, pageable);
    }

    // ========================
    // OPERACIONES ESPECÍFICAS DE CONTACTO
    // ========================

    @Override
    public Contact createCustomerContact(Customer customer, String firstName, String lastName,
                                         Contact.ContactType contactType) {
        logger.debug("Creating customer contact: {} {} for customer: {}",
                firstName, lastName, customer.getName());

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        Contact contact = new Contact();
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setContactType(contactType);
        contact.setCustomer(customer);

        // Validar y guardar
        Contact savedContact = save(contact);

        logger.info("Customer contact created: {} for customer: {}",
                savedContact.getFullName(), customer.getName());

        return savedContact;
    }

    @Override
    public Contact createSupplierContact(Supplier supplier, String firstName, String lastName,
                                         Contact.ContactType contactType) {
        logger.debug("Creating supplier contact: {} {} for supplier: {}",
                firstName, lastName, supplier.getName());

        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }

        Contact contact = new Contact();
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setContactType(contactType);
        contact.setSupplier(supplier);

        // Validar y guardar
        Contact savedContact = save(contact);

        logger.info("Supplier contact created: {} for supplier: {}",
                savedContact.getFullName(), supplier.getName());

        return savedContact;
    }

    @Override
    public Contact transferContactToSupplier(Long contactId, Supplier supplier) {
        logger.debug("Transferring contact {} to supplier: {}", contactId, supplier.getName());

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found with ID: " + contactId));

        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }

        // Verificar que el contacto pertenezca a la misma empresa
        Company contactCompany = contact.isCustomerContact() ?
                contact.getCustomer().getCompany() : contact.getSupplier().getCompany();

        if (!contactCompany.getId().equals(supplier.getCompany().getId())) {
            throw new IllegalArgumentException("Contact and supplier must belong to the same company");
        }

        String oldParent = contact.isCustomerContact() ?
                contact.getCustomer().getName() : contact.getSupplier().getName();

        // Transferir
        contact.setCustomer(null);
        contact.setSupplier(supplier);

        Contact savedContact = contactRepository.save(contact);

        logger.info("Contact {} transferred from {} to supplier: {}",
                contact.getFullName(), oldParent, supplier.getName());

        return savedContact;
    }

    @Override
    public Contact transferContactToCustomer(Long contactId, Customer customer) {
        logger.debug("Transferring contact {} to customer: {}", contactId, customer.getName());

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found with ID: " + contactId));

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        // Verificar que el contacto pertenezca a la misma empresa
        Company contactCompany = contact.isCustomerContact() ?
                contact.getCustomer().getCompany() : contact.getSupplier().getCompany();

        if (!contactCompany.getId().equals(customer.getCompany().getId())) {
            throw new IllegalArgumentException("Contact and customer must belong to the same company");
        }

        String oldParent = contact.isCustomerContact() ?
                contact.getCustomer().getName() : contact.getSupplier().getName();

        // Transferir
        contact.setSupplier(null);
        contact.setCustomer(customer);

        Contact savedContact = contactRepository.save(contact);

        logger.info("Contact {} transferred from {} to customer: {}",
                contact.getFullName(), oldParent, customer.getName());

        return savedContact;
    }

    // ========================
    // VALIDACIONES DE NEGOCIO
    // ========================

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Company company, Long excludeId) {
        if (!StringUtils.hasText(email)) {
            return true; // Email vacío es válido
        }

        if (excludeId != null) {
            return !contactRepository.existsByEmailAndCompanyExcludingId(email, company, excludeId);
        } else {
            return !contactRepository.existsByEmailAndCompany(email, company);
        }
    }

    @Override
    public void validateContact(Contact contact) {
        logger.debug("Validating contact: {} {}", contact.getFirstName(), contact.getLastName());

        // Validaciones básicas
        if (!StringUtils.hasText(contact.getFirstName())) {
            throw new IllegalArgumentException("First name is required");
        }

        if (!StringUtils.hasText(contact.getLastName())) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (contact.getContactType() == null) {
            throw new IllegalArgumentException("Contact type is required");
        }

        // Validar relación exclusiva
        validateExclusiveRelation(contact);

        // Validar unicidad de email si está presente
        if (StringUtils.hasText(contact.getEmail())) {
            Company company = contact.isCustomerContact() ?
                    contact.getCustomer().getCompany() : contact.getSupplier().getCompany();

            if (!isEmailUnique(contact.getEmail(), company, contact.getId())) {
                throw new IllegalArgumentException("Email already exists for another contact in this company");
            }
        }

        logger.debug("Contact validation passed");
    }

    @Override
    public void validateExclusiveRelation(Contact contact) {
        boolean hasCustomer = contact.getCustomer() != null;
        boolean hasSupplier = contact.getSupplier() != null;

        if (!hasCustomer && !hasSupplier) {
            throw new IllegalArgumentException("Contact must belong to either a Customer or a Supplier");
        }

        if (hasCustomer && hasSupplier) {
            throw new IllegalArgumentException("Contact cannot belong to both Customer and Supplier");
        }

        logger.debug("Exclusive relation validation passed - Contact belongs to: {}",
                hasCustomer ? "Customer" : "Supplier");
    }

    // ========================
    // ESTADÍSTICAS
    // ========================

    @Override
    @Transactional(readOnly = true)
    public long countByCompany(Company company) {
        return contactRepository.countByCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCustomer(Customer customer) {
        return contactRepository.findByCustomer(customer).size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countBySupplier(Supplier supplier) {
        return contactRepository.findBySupplier(supplier).size();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getContactCountByType(Company company) {
        logger.debug("Getting contact count by type for company: {}", company.getName());
        return contactRepository.countByTypeAndCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findPrimaryContactsByCompany(Company company) {
        logger.debug("Finding primary (COMMERCIAL) contacts for company: {}", company.getName());

        // Obtener todos los contactos COMMERCIAL de la empresa
        return contactRepository.findByCompanyWithFilters(
                company,
                Contact.ContactType.COMMERCIAL,
                null, // tanto customer como supplier contacts
                null, // sin filtro de búsqueda
                null  // sin paginación
        ).getContent();
    }
}