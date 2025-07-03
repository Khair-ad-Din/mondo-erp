package com.mondo.erp.core.controller.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.crm.Customer;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.crm.CustomerService;
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

import java.util.Optional;

@Controller
@RequestMapping("/crm/customers")
@PreAuthorize("hasAuthority('CRM_READ')")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final UserService userService;

    @Autowired
    public CustomerController(CustomerService customerService, UserService userService) {
        this.customerService = customerService;
        this.userService = userService;
    }

    // ========================
    // LISTADO DE CLIENTES
    // ========================

    @GetMapping
    public String listCustomer(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Displaying customer list for user: {}", userDetails.getUsername());

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();

        // Obtener clientes con filtros
        Page<Customer> customers = customerService.findWithFilters(company, active, search, pageable);

        // Estadisticas para el dashboard
        long totalCustomers = customerService.countByCompany(company);
        long activeCustomers = customerService.countActiveByCompany(company);

        // Preparación del modelo
        model.addAttribute("customers", customers);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("activeCustomers", activeCustomers);
        model.addAttribute("inactiveCustomers", totalCustomers - activeCustomers);
        model.addAttribute("currentActive", active);
        model.addAttribute("currentSearch", search);
        model.addAttribute("activeLink", "crm-customers");

        return "crm/customer/list";
    }

    // ========================
    // VISTA DETALLE
    // ========================

    // Muestra el detalle de un cliente
    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails){
        logger.debug("Viewing customer detail for ID: {}", id);

        User user = getUserFromPrincipal(userDetails);
        Customer customer = getCustomerByIdAndCompany(id, user.getCompany());

        model.addAttribute("customer", customer);
        model.addAttribute("activeLink", "crm-customers");

        return "crm/customer/detail";
    }

    // ========================
    // FORMULARIO NUEVO CLIENTE
    // ========================

    // Muestra el formulario para crear un nuevo cliente
    @GetMapping("/new")
    @PreAuthorize("hasAuthority('CRM_CREATE')")
    public String newCustomerForm(Model model, @AuthenticationPrincipal UserDetails userDetails){
        logger.debug("Displaying new customer form");

        User user = getUserFromPrincipal(userDetails);

        Customer customer = new Customer();
        customer.setCompany(user.getCompany());

        model.addAttribute("customer", customer);
        model.addAttribute("isEdit", false);
        model.addAttribute("activeLink", "crm-customers");

        return "crm/customer/form";
    }

    // ========================
    // FORMULARIO EDITAR CLIENTE
    // ========================

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('CRM_UPDATE')")
    public String editCustomerForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Displaying edit customer form for ID: {}", id);

        User user = getUserFromPrincipal(userDetails);
        Customer customer = getCustomerByIdAndCompany(id, user.getCompany());

        model.addAttribute("customer", customer);
        model.addAttribute("isEdit", true);
        model.addAttribute("activeLink", "crm-customers");

        return "crm/customer/form";
    }

    // ========================
    // GUARDAR CLIENTE
    // ========================

    // Procesa el formulario de cliente (nuevo o edición)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CRM_CREATE', 'CRM_UPDATE')")
    public String saveCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Processing customer form submission for: {}", customer.getName());

        User user = getUserFromPrincipal(userDetails);
        boolean isEdit = customer.getId() != null;

        // Verificar que pertenece a la empresa del usuario
        customer.setCompany(user.getCompany());

        // Validaciones adicionales
        if (result.hasErrors()) {
            logger.warn("Customer form validation errors: {}", result.getAllErrors());
            model.addAttribute("customer", customer);
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("activeLink", "crm-customers");
            return "crm/customer/form";
        }

        try {
            // Validaciones de negocio personalizadas
            customerService.validateCustomer(customer);

            // Guardar cliente
            Customer savedCustomer = customerService.save(customer);

            // Mensaje de éxito
            String message = isEdit ?
                    "Customer updated successfully: " + savedCustomer.getName() :
                    "Customer created successfully: " + savedCustomer.getName();

            redirectAttributes.addFlashAttribute("successMessage", message);
            logger.info("Customer {} successfully: {}", isEdit ? "updated" : "created", savedCustomer.getName());

            return "redirect:/crm/customers/" + savedCustomer.getId();

        } catch (IllegalArgumentException e) {
            logger.warn("Customer validation error: {}", e.getMessage());
            result.rejectValue("name", "validation.error", e.getMessage());

            model.addAttribute("customer", customer);
            model.addAttribute("isEdit", isEdit);
            model.addAttribute("activeLink", "crm-customers");
            return "crm/customer/form";

        } catch (Exception e) {
            logger.error("Error saving customer: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving customer: " + e.getMessage());
            return "redirect:/crm/customers";
        }
    }

    // ========================
    // TOGGLE ACTIVE/INACTIVE
    // ========================

    // Activa o desactiva un cliente
    @PostMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('CRM_UPDATE')")
    public String toggleActive(@PathVariable Long id,
                               RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Toggling active status for customer ID: {}", id);

        try {
            User user = getUserFromPrincipal(userDetails);

            // Verificar que el cliente pertenece a la empresa del usuario
            Customer customer = getCustomerByIdAndCompany(id, user.getCompany());

            // Toggle status
            Customer updatedCustomer = customerService.toggleActive(id);

            String message = updatedCustomer.getActive() ?
                    "Customer activated: " + updatedCustomer.getName() :
                    "Customer deactivated: " + updatedCustomer.getName();

            redirectAttributes.addFlashAttribute("successMessage", message);
            logger.info("Customer {} status changed to: {}", updatedCustomer.getName(), updatedCustomer.getActive());

        } catch (Exception e) {
            logger.error("Error toggling customer status: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing customer status: " + e.getMessage());
        }

        return "redirect:/crm/customers";
    }

    // ========================
    // ELIMINAR CLIENTE
    // ========================

    // Elimina un cliente
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('CRM_DELETE')")
    public String deleteCustomer(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Deleting customer with ID: {}", id);

        try {
            User user = getUserFromPrincipal(userDetails);

            // Verificar que el cliente pertenece a la empresa del usuario
            Customer customer = getCustomerByIdAndCompany(id, user.getCompany());

            // Verificar si puede ser eliminado
            if (!customerService.canBeDeleted(id)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Customer cannot be deleted because it has associated transactions");
                return "redirect:/crm/customers";
            }

            String customerName = customer.getName();
            customerService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage", "Customer deleted: " + customerName);
            logger.info("Customer deleted successfully: {}", customerName);

        } catch (Exception e) {
            logger.error("Error deleting customer: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting customer: " + e.getMessage());
        }

        return "redirect:/crm/customers";
    }

    // ========================
    // MÉTODOS DE UTILIDAD PRIVADOS
    // ========================

    // Obtiene el usuario desde el principal autenticado
    private User getUserFromPrincipal(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found: " + userDetails.getUsername()));
    }

    /**
     * Obtiene un cliente por ID verificando que pertenece a la empresa del usuario
     */
    private Customer getCustomerByIdAndCompany(Long id, Company company) {
        Optional<Customer> customerOpt = customerService.findById(id);

        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer not found with ID: " + id);
        }

        Customer customer = customerOpt.get();

        if (!customer.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("Customer does not belong to your company");
        }

        return customer;
    }

    // ========================
    // EXCEPTION HANDLER
    // ========================

    // Maneja excepciones no controladas
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        logger.warn("Illegal argument exception: {}", e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/crm/customers";
    }
}
