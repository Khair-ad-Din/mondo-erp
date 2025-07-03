package com.mondo.erp.core.repository.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // ========================
    // CONSULTAS BÁSICAS POR COMPANY
    // ========================

    List<Supplier> findByCompany(Company company);

    List<Supplier> findByCompanyAndActive(Company company, Boolean isActive);

    Page<Supplier> findByCompany(Company company, Pageable pageable);

    Page<Supplier> findByCompanyAndActive(Company company, Boolean isActive, Pageable pageable);

    // ========================
    // BÚSQUEDAS POR CAMPOS ESPECÍFICOS
    // ========================

    Optional<Supplier> findByTaxIdAndCompany(String taxId, Company company);

    Optional<Supplier> findByEmailAndCompany(String email, Company company);

    boolean existsByTaxIdAndCompany(String taxId, Company company);

    boolean existsByEmailAndCompany(String email, Company company);

    // ========================
    // BÚSQUEDAS POR CATEGORÍA DE PROVEEDOR
    // ========================

    // Encuentra proveedores por categoría
    List<Supplier> findByCompanyAndSupplierCategory(Company company, String supplierCategory);

    // Encuentra proveedores activos por categoría
    List<Supplier> findByCompanyAndSupplierCategoryAndActive(Company company,
                                                              String supplierCategory,
                                                              Boolean isActive);

    // Obtiene todas las categorías de proveedores de una empresa (distinct)
    @Query("SELECT DISTINCT s.supplierCategory FROM Supplier s " +
            "WHERE s.company = :company AND s.supplierCategory IS NOT NULL " +
            "ORDER BY s.supplierCategory")
    List<String> findDistinctCategoriesByCompany(@Param("company") Company company);

    // ========================
    // BÚSQUEDAS AVANZADAS
    // ========================

    @Query("SELECT s FROM Supplier s WHERE s.company = :company AND " +
            "(LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.taxId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.supplierCategory) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Supplier> findByCompanyAndSearchTerm(@Param("company") Company company,
                                              @Param("searchTerm") String searchTerm,
                                              Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.company = :company " +
            "AND (:active IS NULL OR s.active = :active) " +
            "AND (:category IS NULL OR s.supplierCategory = :category) " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(s.taxId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY s.name ASC")
    Page<Supplier> findByCompanyWithFilters(@Param("company") Company company,
                                            @Param("active") Boolean active,
                                            @Param("category") String category,
                                            @Param("searchTerm") String searchTerm,
                                            Pageable pageable);

    // ========================
    // VALIDACIONES
    // ========================

    @Query("SELECT COUNT(s) > 0 FROM Supplier s WHERE s.taxId = :taxId " +
            "AND s.company = :company AND s.id != :excludeId")
    boolean existsByTaxIdAndCompanyExcludingId(@Param("taxId") String taxId,
                                               @Param("company") Company company,
                                               @Param("excludeId") Long excludeId);

    @Query("SELECT COUNT(s) > 0 FROM Supplier s WHERE s.email = :email " +
            "AND s.company = :company AND s.id != :excludeId")
    boolean existsByEmailAndCompanyExcludingId(@Param("email") String email,
                                               @Param("company") Company company,
                                               @Param("excludeId") Long excludeId);

    // ========================
    // ESTADÍSTICAS
    // ========================

    long countByCompanyAndActive(Company company, Boolean active);

    @Query("SELECT s.supplierCategory, COUNT(s) FROM Supplier s " +
            "WHERE s.company = :company AND s.supplierCategory IS NOT NULL " +
            "GROUP BY s.supplierCategory ORDER BY COUNT(s) DESC")
    List<Object[]> countByCategory(@Param("company") Company company);

    // ========================
    // PROYECCIONES
    // ========================

    interface SupplierBasicInfo {
        Long getId();
        String getName();
        String getEmail();
        String getTaxId();
        String getSupplierCategory();
        Boolean getActive();
    }

    @Query("SELECT s.id AS id, s.name AS name, s.email AS email, " +
            "s.taxId AS taxId, s.supplierCategory AS supplierCategory, " +
            "s.active AS active FROM Supplier s WHERE s.company = :company")
    List<SupplierBasicInfo> findBasicInfoByCompany(@Param("company") Company company);
}
