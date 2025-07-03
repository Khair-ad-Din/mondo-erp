package com.mondo.erp.core.service.crm.impl;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Supplier;
import com.mondo.erp.core.repository.crm.SupplierRepository;
import com.mondo.erp.core.service.crm.SupplierService;
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
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);

    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // ========================
    // OPERACIONES CRUD BÁSICAS
    // ========================

    @Override
    public Supplier save(Supplier supplier) {
        logger.debug("Saving supplier: {}", supplier.getName());

        // Validar antes de guardar
        validateSupplier(supplier);

        // Normalizar categoría (opcional: convertir a Title Case)
        if (StringUtils.hasText(supplier.getSupplierCategory())) {
            supplier.setSupplierCategory(normalizeCategory(supplier.getSupplierCategory()));
        }

        // Guardar y retornar
        Supplier savedSupplier = supplierRepository.save(supplier);
        logger.info("Supplier saved successfully with ID: {}", savedSupplier.getId());

        return savedSupplier;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Supplier> findById(Long id) {
        logger.debug("Finding supplier by ID: {}", id);
        return supplierRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Deleting supplier with ID: {}", id);

        // Verificar si puede ser eliminado
        if (!canBeDeleted(id)) {
            throw new IllegalStateException("Supplier cannot be deleted because it has associated transactions");
        }

        supplierRepository.deleteById(id);
        logger.info("Supplier with ID {} deleted successfully", id);
    }

    @Override
    public void delete(Supplier supplier) {
        logger.debug("Deleting supplier: {}", supplier.getName());

        if (!canBeDeleted(supplier.getId())) {
            throw new IllegalStateException("Supplier cannot be deleted because it has associated transactions");
        }

        supplierRepository.delete(supplier);
        logger.info("Supplier {} deleted successfully", supplier.getName());
    }

    // ========================
    // CONSULTAS POR COMPANY
    // ========================

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> findByCompany(Company company) {
        logger.debug("Finding all suppliers for company: {}", company.getName());
        return supplierRepository.findByCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> findActiveByCompany(Company company) {
        logger.debug("Finding active suppliers for company: {}", company.getName());
        return supplierRepository.findByCompanyAndActive(company, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Supplier> findByCompany(Company company, Pageable pageable) {
        logger.debug("Finding suppliers for company with pagination: {}", company.getName());
        return supplierRepository.findByCompany(company, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Supplier> findActiveByCompany(Company company, Pageable pageable) {
        logger.debug("Finding active suppliers for company with pagination: {}", company.getName());
        return supplierRepository.findByCompanyAndActive(company, true, pageable);
    }

    // ========================
    // BÚSQUEDAS ESPECÍFICAS
    // ========================

    @Override
    @Transactional(readOnly = true)
    public Optional<Supplier> findByTaxIdAndCompany(String taxId, Company company) {
        logger.debug("Finding supplier by taxId: {} and company: {}", taxId, company.getName());
        return supplierRepository.findByTaxIdAndCompany(taxId, company);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Supplier> findByEmailAndCompany(String email, Company company) {
        logger.debug("Finding supplier by email: {} and company: {}", email, company.getName());
        return supplierRepository.findByEmailAndCompany(email, company);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Supplier> findWithFilters(Company company, Boolean active, String category, String searchTerm, Pageable pageable) {
        logger.debug("Finding suppliers with filters - Company: {}, Active: {}, Category: {}, Search: {}",
                company.getName(), active, category, searchTerm);
        return supplierRepository.findByCompanyWithFilters(company, active, category, searchTerm, pageable);
    }

    // ========================
    // BÚSQUEDAS POR CATEGORÍA
    // ========================

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> findByCategory(Company company, String category) {
        logger.debug("Finding suppliers by category: {} for company: {}", category, company.getName());
        return supplierRepository.findByCompanyAndSupplierCategory(company, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> findActiveByCategoryAndCompany(Company company, String category) {
        logger.debug("Finding active suppliers by category: {} for company: {}", category, company.getName());
        return supplierRepository.findByCompanyAndSupplierCategoryAndActive(company, category, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllCategoriesByCompany(Company company) {
        logger.debug("Finding all supplier categories for company: {}", company.getName());
        return supplierRepository.findDistinctCategoriesByCompany(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getSupplierCountByCategory(Company company) {
        logger.debug("Getting supplier count by category for company: {}", company.getName());
        return supplierRepository.countByCategory(company);
    }

    // ========================
    // VALIDACIONES DE NEGOCIO
    // ========================

    @Override
    @Transactional(readOnly = true)
    public boolean isTaxIdUnique(String taxId, Company company, Long excludeId) {
        if (!StringUtils.hasText(taxId)) {
            return true; // Tax ID vacío es válido
        }

        if (excludeId != null) {
            return !supplierRepository.existsByTaxIdAndCompanyExcludingId(taxId, company, excludeId);
        } else {
            return !supplierRepository.existsByTaxIdAndCompany(taxId, company);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Company company, Long excludeId) {
        if (!StringUtils.hasText(email)) {
            return true; // Email vacío es válido
        }

        if (excludeId != null) {
            return !supplierRepository.existsByEmailAndCompanyExcludingId(email, company, excludeId);
        } else {
            return !supplierRepository.existsByEmailAndCompany(email, company);
        }
    }

    @Override
    public void validateSupplier(Supplier supplier) {
        logger.debug("Validating supplier: {}", supplier.getName());

        // Validación básica
        if (supplier.getCompany() == null) {
            throw new IllegalArgumentException("Company is required");
        }

        if (!StringUtils.hasText(supplier.getName())) {
            throw new IllegalArgumentException("Supplier name is required");
        }

        // Validar unicidad de Tax ID
        if (StringUtils.hasText(supplier.getTaxId())) {
            if (!isTaxIdUnique(supplier.getTaxId(), supplier.getCompany(), supplier.getId())) {
                throw new IllegalArgumentException("Tax ID already exists for another supplier in this company");
            }
        }

        // Validar unicidad de email
        if (StringUtils.hasText(supplier.getEmail())) {
            if (!isEmailUnique(supplier.getEmail(), supplier.getCompany(), supplier.getId())) {
                throw new IllegalArgumentException("Email already exists for another supplier in this company");
            }
        }

        // Validaciones específicas de supplier
        if (StringUtils.hasText(supplier.getBankAccount())) {
            validateBankAccount(supplier.getBankAccount());
        }

        logger.debug("Supplier validation passed");
    }

    // ========================
    // OPERACIONES DE NEGOCIO
    // ========================

    @Override
    public Supplier toggleActive(Long supplierId) {
        logger.debug("Toggling active status for supplier ID: {}", supplierId);

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with ID: " + supplierId));

        supplier.setActive(!supplier.getActive());
        Supplier savedSupplier = supplierRepository.save(supplier);

        logger.info("Supplier ID {} active status changed to: {}", supplierId, savedSupplier.getActive());
        return savedSupplier;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canBeDeleted(Long supplierId) {
        // TODO: Implementar validación cuando tengamos módulos de compras/facturas
        logger.debug("Checking if supplier {} can be deleted", supplierId);

        // Regla de negocio: por ahora permitir eliminación
        // En el futuro verificar:
        // - No tenga órdenes de compra pendientes
        // - No tenga facturas sin pagar
        // - etc.

        return true;
    }

    // ========================
    // ESTADÍSTICAS
    // ========================

    @Override
    @Transactional(readOnly = true)
    public long countActiveByCompany(Company company) {
        return supplierRepository.countByCompanyAndActive(company, true);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCompany(Company company) {
        return supplierRepository.countByCompanyAndActive(company, true) +
                supplierRepository.countByCompanyAndActive(company, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findTopCategoriesByCompany(Company company, int limit) {
        logger.debug("Finding top {} categories for company: {}", limit, company.getName());

        return supplierRepository.countByCategory(company)
                .stream()
                .limit(limit)
                .map(row -> (String) row[0]) // row[0] es la categoría, row[1] es el count
                .collect(Collectors.toList());
    }

    // ========================
    // MÉTODOS PRIVADOS DE UTILIDAD
    // ========================

    // Normaliza el nombre de la categoría (convierte a Title Case)
    private String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return category;
        }

        // Convertir a Title Case: primera letra mayúscula, resto minúscula
        return category.trim().substring(0, 1).toUpperCase() +
                category.trim().substring(1).toLowerCase();
    }

    // Valida formato de cuenta bancaria (implementación básica)
    private void validateBankAccount(String bankAccount) {
        // Implementación básica - puedes expandir según necesidades del país
        if (bankAccount.length() < 10) {
            throw new IllegalArgumentException("Bank account number too short");
        }

        // Verificar que solo contenga números, espacios y guiones
        if (!bankAccount.matches("[0-9\\s\\-]+")) {
            throw new IllegalArgumentException("Bank account contains invalid characters");
        }
    }
}