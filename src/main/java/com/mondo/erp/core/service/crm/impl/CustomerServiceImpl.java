package com.mondo.erp.core.service.crm.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Customer;
import com.mondo.erp.core.repository.crm.CustomerRepository;
import com.mondo.erp.core.service.crm.CustomerService;
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
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer save(Customer customer) {
        logger.debug("Saving customer: {}", customer.getName());

        validateCustomer(customer);

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer saved successfully with ID: {}", savedCustomer.getId());

        return savedCustomer;
    }

    @Override
    @Transactional
    public Optional<Customer> findById(Long id) {
        logger.debug("Finding customer by ID: {}", id);
        return customerRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Deleting customer with ID: {}", id);
        if(!canBeDeleted(id)) {
            throw new IllegalStateException("Customer cannot be deleted because it has associated transactions");
        }

        customerRepository.deleteById(id);
        logger.info("Customer with ID {} has been deleted successfully", id);
    }

    @Override
    public void delete(Customer customer) {
        logger.debug("Deleting customer: {}", customer.getName());
        if(!canBeDeleted(customer.getId())){
            throw new IllegalStateException("Customer cannot be deleted because it has associated transactions");
        }

        customerRepository.delete(customer);
        logger.info("Customer {} deleted successfully", customer.getName());
    }

    // ========================
    // CONSULTAS POR COMPANY
    // ========================

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findByCompany(Company company) {
        logger.debug("Finding all customers for company: {}", company.getName());
        return customerRepository.findByCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findActiveByCompany(Company company) {
        logger.debug("Finding active customers by company: {}", company.getName());
        return customerRepository.findByCompanyAndActive(company, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findByCompany(Company company, Pageable pageable) {
        logger.debug("Finding customers for company with pagination: {}", company.getName());
        return customerRepository.findByCompany(company, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findActiveByCompany(Company company, Pageable pageable) {
        logger.debug("Finding active customers by company with pagination: {}", company.getName());
        return customerRepository.findByCompanyAndActive(company, true, pageable);
    }

    // ========================
    // BÚSQUEDAS ESPECÍFICAS
    // ========================

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByTaxIdAndCompany(String taxId, Company company) {
        logger.debug("Finding customer by Tax ID: {} and Company: {}", taxId, company.getName());
        return customerRepository.findByTaxIdAndCompany(taxId, company);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByEmailAndCompany(String email, Company company) {
        logger.debug("Finding customer by Email: {} and Company: {}", email, company.getName());
        return customerRepository.findByEmailAndCompany(email, company);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findWithFilters(Company company, Boolean isActive, String searchTerm, Pageable pageable) {
        logger.debug("Finding customers with filters - Company: {}, Active: {}, Search: {}", company.getName(), isActive, searchTerm);
        return customerRepository.findByCompanyWithFilters(company, isActive, searchTerm, pageable);
    }

    // ========================
    // VALIDACIONES DE NEGOCIO
    // ========================

    @Override
    @Transactional(readOnly = true)
    public boolean isTaxIdUnique(String taxId, Company company, Long excludeId) {
        if(!StringUtils.hasText(taxId)){
            return true; // Tax ID vacío es válido (no obligatorio)
        }

        if(excludeId != null) {
            return !customerRepository.existsByTaxIdAndCompanyExcludingId(taxId, company, excludeId);
        } else {
            return !customerRepository.existsByTaxIdAndCompany(taxId, company);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Company company, Long excludeId) {
        if (!StringUtils.hasText(email)) {
            return true; // Email vacío es válido
        }

        if (excludeId != null) {
            return !customerRepository.existsByEmailAndCompanyExcludingId(email, company, excludeId);
        } else {
            return !customerRepository.existsByEmailAndCompany(email, company);
        }
    }

    @Override
    public void validateCustomer(Customer customer) {
        logger.debug("Validating customer: {}", customer.getName());

        // Validación básica
        if (customer.getCompany() == null) {
            throw new IllegalArgumentException("Company is required");
        }

        if (!StringUtils.hasText(customer.getName())) {
            throw new IllegalArgumentException("Customer name is required");
        }

        // Validar unicidad de Tax ID
        if (StringUtils.hasText(customer.getTaxId())) {
            if (!isTaxIdUnique(customer.getTaxId(), customer.getCompany(), customer.getId())) {
                throw new IllegalArgumentException("Tax ID already exists for another customer in this company");
            }
        }

        // Validar unicidad de email
        if (StringUtils.hasText(customer.getEmail())) {
            if (!isEmailUnique(customer.getEmail(), customer.getCompany(), customer.getId())) {
                throw new IllegalArgumentException("Email already exists for another customer in this company");
            }
        }

        logger.debug("Customer validation passed");
    }

    // ========================
    // OPERACIONES DE NEGOCIO
    // ========================

    @Override
    public Customer toggleActive(Long customerId) {
        logger.debug("Toggling active status for customer ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer with ID: " + customerId + " not found"));

        customer.setActive(!customer.getActive());
        Customer savedCustomer = customerRepository.save(customer);

        logger.info("Customer ID {} active status changed to: {}", customerId, savedCustomer.getActive());
        return savedCustomer;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canBeDeleted(Long customerId) {
        // TODO: Implementar validación cuando tenga módulos de ventas/facturas
        // Por ahora, verificar que no tenga contactos críticos o permitir eliminación en cascada
        logger.debug("Checking if customer {} can be deleted", customerId);

        // Regla de negocio: por ahora permitir eliminación (contacts se eliminan en cascada)
        // En el futuro verificar:
        // - No tenga facturas pendientes
        // - No tenga pedidos en curso
        // - etc.
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByCompany(Company company) {
        return customerRepository.countByCompanyAndActive(company, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCompany(Company company) {
        return customerRepository.countByCompanyAndActive(company, true) +
                customerRepository.countByCompanyAndActive(company, false);
    }
}
