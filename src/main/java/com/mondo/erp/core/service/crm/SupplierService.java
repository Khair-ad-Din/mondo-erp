package com.mondo.erp.core.service.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SupplierService {

    // ========================
    // OPERACIONES CRUD BÁSICAS
    // ========================

    Supplier save(Supplier supplier);

    Optional<Supplier> findById(Long id);

    void deleteById(Long id);

    void delete(Supplier supplier);

    // ========================
    // CONSULTAS POR COMPANY
    // ========================

    List<Supplier> findByCompany(Company company);

    List<Supplier> findActiveByCompany(Company company);

    Page<Supplier> findByCompany(Company company, Pageable pageable);

    Page<Supplier> findActiveByCompany(Company company, Pageable pageable);

    // ========================
    // BÚSQUEDAS ESPECÍFICAS
    // ========================

    Optional<Supplier> findByTaxIdAndCompany(String taxId, Company company);

    Optional<Supplier> findByEmailAndCompany(String email, Company company);

    Page<Supplier> findWithFilters(Company company, Boolean active, String category, String searchTerm, Pageable pageable);

    // ========================
    // BÚSQUEDAS POR CATEGORÍA
    // ========================

    // Obtiene proveedores por categoría

    List<Supplier> findByCategory(Company company, String category);

    // Obtiene proveedores activos por categoría
    List<Supplier> findActiveByCategoryAndCompany(Company company, String category);

    // Obtiene todas las categorías disponibles en una empresa
    List<String> findAllCategoriesByCompany(Company company);

    // Obtiene estadísticas de proveedores por categoría
    List<Object[]> getSupplierCountByCategory(Company company);

    // ========================
    // VALIDACIONES DE NEGOCIO
    // ========================

    boolean isTaxIdUnique(String taxId, Company company, Long excludeId);

    boolean isEmailUnique(String email, Company company, Long excludeId);

    void validateSupplier(Supplier supplier);

    // ========================
    // OPERACIONES DE NEGOCIO
    // ========================

    Supplier toggleActive(Long supplierId);

    boolean canBeDeleted(Long supplierId);

    // ========================
    // ESTADÍSTICAS
    // ========================

    long countActiveByCompany(Company company);

    long countByCompany(Company company);

    // Obtiene las categorías más utilizadas
    List<String> findTopCategoriesByCompany(Company company, int limit);
}