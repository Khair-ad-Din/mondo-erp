package com.mondo.erp.core.repository.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // ========================
    // CONSULTAS BÁSICAS POR COMPANY
    // ========================

    // Encuentra todos los clientes de una empresa específica
    List<Customer> findByCompany(Company company);

    // Encuentra clientes activos de una empresa
    List<Customer> findByCompanyAndActive(Company company, Boolean isActive);

    // Encuentra clientes de una empresa con paginacion
    Page<Customer> findByCompany(Company company, Pageable pageable);

    // Encuentra clientes activos de una empresa con paginación
    Page<Customer> findByCompanyAndActive(Company company, Boolean isActive, Pageable pageable);

    // ========================
    // BÚSQUEDAS POR CAMPOS ESPECÍFICOS
    // ========================

    // Busca cliente por Tax ID y empresa (debe ser único)
    Optional<Customer> findByTaxIdAndCompany (String taxId, Company company);

    // Busca cliente por email y empresa
    Optional<Customer> findByEmailAndCompany (String email, Company company);

    // Verifica si existe un Tax ID en una empresa (para validaciones)
    boolean existsByTaxIdAndCompany (String taxId, Company company);

    //Verifica si existe un email en una empresa
    boolean existsByEmailAndCompany (String email, Company company);

    // ========================
    // BÚSQUEDAS AVANZADAS CON @Query
    // ========================

    //Búsqueda de texto completo en nombre, email o tax_id
    @Query("SELECT c FROM Customer c WHERE c.company = :company AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.taxId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Customer> findByCompanyAndSearchTerm(@Param("company") Company company,
                                              @Param("searchTerm") String searchTerm,
                                              Pageable pageable);

    // Búsqueda avanzada con múltiples filtros
    @Query("SELECT c FROM Customer c WHERE c.company = :company " +
            "AND (:active IS NULL OR c.active = :active) " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(c.taxId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY c.name ASC")
    Page<Customer> findByCompanyWithFilters(@Param("company") Company company,
                                            @Param("active") Boolean active,
                                            @Param("searchTerm") String searchTerm,
                                            Pageable pageable);

    // ========================
    // CONSULTAS PARA VALIDACIONES
    // ========================

    // Verifica unicidad de Tax ID excluyendo un customer específico (para edición)
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.taxId = :taxId " +
            "AND c.company = :company AND c.id != :excludeId")
    boolean existsByTaxIdAndCompanyExcludingId(@Param("taxId") String taxId,
                                               @Param("company") Company company,
                                               @Param("excludeId") Long excludeId);

    // Verifica unicidad de email excluyendo un customer específico
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.email = :email " +
            "AND c.company = :company AND c.id != :excludeId")
    boolean existsByEmailAndCompanyExcludingId(@Param("email") String email,
                                               @Param("company") Company company,
                                               @Param("excludeId") Long excludeId);

    // ========================
    // CONSULTAS DE ESTADÍSTICAS
    // ========================

    // Cuenta clientes activos por empresa
    long countByCompanyAndActive(Company company, Boolean active);

    // ========================
    // PROYECCIONES PARA PERFORMANCE
    // ========================

    // Interfaz para proyección de datos básicos del cliente
    interface CustomerBasicInfo {
        Long getId();
        String getName();
        String getEmail();
        String getTaxId();
        Boolean getActive();
    }

    // Obtiene información básica de clientes (sin cargar relaciones)
    @Query("SELECT c.id AS id, c.name AS name, c.email AS email, " +
            "c.taxId AS taxId, c.active AS active FROM Customer c " +
            "WHERE c.company = :company")
    List<CustomerBasicInfo> findBasicInfoByCompany(@Param("company") Company company);








}
