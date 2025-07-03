package com.mondo.erp.core.service.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.crm.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface CustomerService {

    // ========================
    // OPERACIONES CRUD BÁSICAS
    // ========================

    Customer save(Customer customer);
    Optional<Customer> findById(Long id);
    void deleteById(Long id);
    // Elimina un cliente (con validaciones)
    void delete(Customer customer);

    // ========================
    // CONSULTAS POR COMPANY
    // ========================

    // Obtiene todos los clientes de una empresa
    List<Customer> findByCompany(Company company);

    // Obtiene clientes activos de una empresa
    List<Customer> findActiveByCompany(Company company);

    // Obtiene clientes de una empresa con paginación
    Page<Customer> findByCompany(Company company, Pageable pageable);

    // Obtiene clientes activos con paginación
    Page<Customer> findActiveByCompany(Company company, Pageable pageable);

    // ========================
    // BÚSQUEDAS ESPECÍFICAS
    // ========================

    // Busca cliente por Tax ID y empresa
    Optional<Customer> findByTaxIdAndCompany(String taxId, Company company);

    // Busca cliente por email y empresa
    Optional<Customer> findByEmailAndCompany(String email, Company company);

    // Búsqueda avanzada con filtros
    Page<Customer> findWithFilters(Company company, Boolean isActive, String searchTerm, Pageable pageable);

    // ========================
    // VALIDACIONES DE NEGOCIO
    // ========================

    /**
     * Verifica si un Tax ID es único en la empresa
     * @param taxId Tax ID a verificar
     * @param company Empresa
     * @param excludeId ID del cliente a excluir (para edición), puede ser null
     * @return true si es único
     */
    boolean isTaxIdUnique(String taxId, Company company, Long excludeId);

    // Verifica si un email es único en la empresa
    boolean isEmailUnique(String email, Company company, Long excludeId);

    // Valida un cliente antes de guardarlo
    void validateCustomer(Customer customer);

    // ========================
    // OPERACIONES DE NEGOCIO
    // ========================

    // Activa/Desactiva un cliente
    Customer toggleActive(Long customerId);

    // Verifica si un cliente puede ser eliminado(no tiene transacciones pendientes)
    boolean canBeDeleted(Long customerId);

    // ========================
    // ESTADÍSTICAS
    // ========================

    // Cuenta clientes activos de una empresa
    long countActiveByCompany(Company company);

    // Cuenta total de clientes de una empresa
    long countByCompany(Company company);
}
