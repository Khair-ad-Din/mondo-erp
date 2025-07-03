package com.mondo.erp.core.controller.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.crm.Supplier;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.crm.SupplierService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/crm/suppliers")
@PreAuthorize("hasAuthority('CRM_READ')")
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    private final SupplierService supplierService;
    private final UserService userService;

    @Autowired
    public SupplierController(SupplierService supplierService, UserService userService) {
        this.supplierService = supplierService;
        this.userService = userService;
    }

    // ========================
    // LISTADO DE PROVEEDORES
    // ========================

    /**
     * Muestra la lista de proveedores con paginación y filtros
     */
    @GetMapping
    public String listSuppliers(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Displaying supplier list for user: {}", userDetails.getUsername());

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();

        // Obtener proveedores con filtros
        Page<Supplier> suppliers = supplierService.findWithFilters(company, active, category, search, pageable);

        // Obtener todas las categorías para el filtro
        List<String> categories = supplierService.findAllCategoriesByCompany(company);

        // Estadísticas para el dashboard
        long totalSuppliers = supplierService.countByCompany(company);
        long activeSuppliers = supplierService.countActiveByCompany(company);

        // Estadísticas por categoría
        List<Object[]> categoryStats = supplierService.getSupplierCountByCategory(company);

        // Preparar modelo
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("totalSuppliers", totalSuppliers);
        model.addAttribute("activeSuppliers", activeSuppliers);
        model.addAttribute("inactiveSuppliers", totalSuppliers - activeSuppliers);
        model.addAttribute("currentActive", active);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        model.addAttribute("activeLink", "crm-suppliers");

        return "crm/supplier/list";
    }

    // ========================
    // VISTA DETALLE
    // ========================

    /**
     * Muestra el detalle de un proveedor
     */
    @GetMapping("/{id}")
    public String viewSupplier(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Viewing supplier detail for ID: {}", id);

        User user = getUserFromPrincipal(userDetails);
        Supplier supplier = getSupplierByIdAndCompany(id, user.getCompany());

        model.addAttribute("supplier", supplier);
        model.addAttribute("activeLink", "crm-suppliers");

        return "crm/supplier/detail";
    }

    // ========================
    // FORMULARIO NUEVO PROVEEDOR
    // ========================

    /**
     * Muestra el formulario para crear un nuevo proveedor
     */
    @GetMapping("/new")
    @PreAuthorize("hasAuthority('CRM_CREATE')")
    public String newSupplierForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Displaying new supplier form");

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();

        Supplier supplier = new Supplier();
        supplier.setCompany(company);

        // Obtener categorías existentes para el dropdown
        List<String> categories = supplierService.findAllCategoriesByCompany(company);

        model.addAttribute("supplier", supplier);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeLink", "crm-suppliers");

        return "crm/supplier/form";
    }

    // ========================
    // FORMULARIO EDITAR PROVEEDOR
    // ========================

    /**
     * Muestra el formulario para editar un proveedor existente
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('CRM_UPDATE')")
    public String editSupplierForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Displaying edit supplier form for ID: {}", id);

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();
        Supplier supplier = getSupplierByIdAndCompany(id, company);

        // Obtener categorías existentes para el dropdown
        List<String> categories = supplierService.findAllCategoriesByCompany(company);

        model.addAttribute("supplier", supplier);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeLink", "crm-suppliers");

        return "crm/supplier/form";
    }

    // ========================
    // GUARDAR PROVEEDOR
    // ========================

    /**
     * Procesa el formulario de proveedor (nuevo o edición)
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CRM_CREATE', 'CRM_UPDATE')")
    public String saveSupplier(
            @Valid @ModelAttribute("supplier") Supplier supplier,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Processing supplier form submission for: {}", supplier.getName());

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();
        boolean isEdit = supplier.getId() != null;

        // Verificar que pertenece a la empresa del usuario
        supplier.setCompany(company);

        // Preparar modelo en caso de error
        List<String> categories = supplierService.findAllCategoriesByCompany(company);

        // Validaciones adicionales
        if (result.hasErrors()) {
            logger.warn("Supplier form validation errors: {}", result.getAllErrors());
            model.addAttribute("supplier", supplier);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("activeLink", "crm-suppliers");
            return "crm/supplier/form";
        }

        try {
            // Validaciones de negocio personalizadas
            supplierService.validateSupplier(supplier);

            // Guardar proveedor
            Supplier savedSupplier = supplierService.save(supplier);

            // Mensaje de éxito
            String message = isEdit ?
                    "Supplier updated successfully: " + savedSupplier.getName() :
                    "Supplier created successfully: " + savedSupplier.getName();

            redirectAttributes.addFlashAttribute("successMessage", message);
            logger.info("Supplier {} successfully: {}", isEdit ? "updated" : "created", savedSupplier.getName());

            return "redirect:/crm/suppliers/" + savedSupplier.getId();

        } catch (IllegalArgumentException e) {
            logger.warn("Supplier validation error: {}", e.getMessage());
            result.rejectValue("name", "validation.error", e.getMessage());

            model.addAttribute("supplier", supplier);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("activeLink", "crm-suppliers");
            return "crm/supplier/form";

        } catch (Exception e) {
            logger.error("Error saving supplier: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving supplier: " + e.getMessage());
            return "redirect:/crm/suppliers";
        }
    }

    // ========================
    // TOGGLE ACTIVE/INACTIVE
    // ========================

    /**
     * Activa o desactiva un proveedor
     */
    @PostMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('CRM_UPDATE')")
    public String toggleActive(@PathVariable Long id,
                               RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Toggling active status for supplier ID: {}", id);

        try {
            User user = getUserFromPrincipal(userDetails);

            // Verificar que el proveedor pertenece a la empresa del usuario
            Supplier supplier = getSupplierByIdAndCompany(id, user.getCompany());

            // Toggle status
            Supplier updatedSupplier = supplierService.toggleActive(id);

            String message = updatedSupplier.getActive() ?
                    "Supplier activated: " + updatedSupplier.getName() :
                    "Supplier deactivated: " + updatedSupplier.getName();

            redirectAttributes.addFlashAttribute("successMessage", message);
            logger.info("Supplier {} status changed to: {}", updatedSupplier.getName(), updatedSupplier.getActive());

        } catch (Exception e) {
            logger.error("Error toggling supplier status: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing supplier status: " + e.getMessage());
        }

        return "redirect:/crm/suppliers";
    }

    // ========================
    // ELIMINAR PROVEEDOR
    // ========================

    /**
     * Elimina un proveedor
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('CRM_DELETE')")
    public String deleteSupplier(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Deleting supplier with ID: {}", id);

        try {
            User user = getUserFromPrincipal(userDetails);

            // Verificar que el proveedor pertenece a la empresa del usuario
            Supplier supplier = getSupplierByIdAndCompany(id, user.getCompany());

            // Verificar si puede ser eliminado
            if (!supplierService.canBeDeleted(id)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Supplier cannot be deleted because it has associated transactions");
                return "redirect:/crm/suppliers";
            }

            String supplierName = supplier.getName();
            supplierService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage", "Supplier deleted: " + supplierName);
            logger.info("Supplier deleted successfully: {}", supplierName);

        } catch (Exception e) {
            logger.error("Error deleting supplier: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting supplier: " + e.getMessage());
        }

        return "redirect:/crm/suppliers";
    }

    // ========================
    // FUNCIONALIDADES ESPECÍFICAS DE SUPPLIER
    // ========================

    /**
     * Muestra proveedores filtrados por categoría
     */
    @GetMapping("/category/{category}")
    public String suppliersByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Displaying suppliers for category: {}", category);

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();

        // Obtener proveedores de la categoría
        List<Supplier> suppliers = supplierService.findActiveByCategoryAndCompany(company, category);

        // Obtener todas las categorías para navegación
        List<String> allCategories = supplierService.findAllCategoriesByCompany(company);

        model.addAttribute("suppliers", suppliers);
        model.addAttribute("currentCategory", category);
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("activeLink", "crm-suppliers");

        return "crm/supplier/by-category";
    }

    /**
     * API endpoint para obtener categorías (para AJAX)
     */
    @GetMapping("/categories")
    @ResponseBody
    public List<String> getCategories(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromPrincipal(userDetails);
        return supplierService.findAllCategoriesByCompany(user.getCompany());
    }

    /**
     * Muestra estadísticas de proveedores por categoría
     */
    @GetMapping("/stats")
    public String supplierStats(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Displaying supplier statistics");

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();

        // Obtener estadísticas
        List<Object[]> categoryStats = supplierService.getSupplierCountByCategory(company);
        List<String> topCategories = supplierService.findTopCategoriesByCompany(company, 5);

        long totalSuppliers = supplierService.countByCompany(company);
        long activeSuppliers = supplierService.countActiveByCompany(company);

        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("topCategories", topCategories);
        model.addAttribute("totalSuppliers", totalSuppliers);
        model.addAttribute("activeSuppliers", activeSuppliers);
        model.addAttribute("inactiveSuppliers", totalSuppliers - activeSuppliers);
        model.addAttribute("activeLink", "crm-suppliers");

        return "crm/supplier/stats";
    }

    // ========================
    // MÉTODOS DE UTILIDAD PRIVADOS
    // ========================

    /**
     * Obtiene el usuario desde el principal autenticado
     */
    private User getUserFromPrincipal(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found: " + userDetails.getUsername()));
    }

    /**
     * Obtiene un proveedor por ID verificando que pertenece a la empresa del usuario
     */
    private Supplier getSupplierByIdAndCompany(Long id, Company company) {
        Optional<Supplier> supplierOpt = supplierService.findById(id);

        if (supplierOpt.isEmpty()) {
            throw new IllegalArgumentException("Supplier not found with ID: " + id);
        }

        Supplier supplier = supplierOpt.get();

        if (!supplier.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("Supplier does not belong to your company");
        }

        return supplier;
    }

    // ========================
    // EXCEPTION HANDLER
    // ========================

    /**
     * Maneja excepciones no controladas
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        logger.warn("Illegal argument exception: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/crm/suppliers";
    }
}