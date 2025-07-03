package com.mondo.erp.core.controller.crm;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.model.crm.Contact;
import com.mondo.erp.core.model.crm.Customer;
import com.mondo.erp.core.model.crm.Supplier;
import com.mondo.erp.core.service.UserService;
import com.mondo.erp.core.service.crm.ContactService;
import com.mondo.erp.core.service.crm.CustomerService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/crm/contacts")
@PreAuthorize("hasAuthority('CRM_READ')")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;
    private final CustomerService customerService;
    private final SupplierService supplierService;
    private final UserService userService;

    @Autowired
    public ContactController(ContactService contactService,
                             CustomerService customerService,
                             SupplierService supplierService,
                             UserService userService) {
        this.contactService = contactService;
        this.customerService = customerService;
        this.supplierService = supplierService;
        this.userService = userService;
    }

    // ========================
    // LISTADO DE CONTACTOS
    // ========================

    /**
     * Muestra la lista de contactos con paginación y filtros
     */
    @GetMapping
    public String listContacts(
            @RequestParam(required = false) Contact.ContactType contactType,
            @RequestParam(required = false) Boolean isCustomerContact,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Displaying contact list for user: {}", userDetails.getUsername());

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();

        // Obtener contactos con filtros
        Page<Contact> contacts = contactService.findWithFilters(company, contactType, isCustomerContact, search, pageable);

        // Estadísticas
        long totalContacts = contactService.countByCompany(company);
        List<Object[]> contactTypeStats = contactService.getContactCountByType(company);

        // Datos para filtros
        List<Contact.ContactType> contactTypes = Arrays.asList(Contact.ContactType.values());

        model.addAttribute("contacts", contacts);
        model.addAttribute("totalContacts", totalContacts);
        model.addAttribute("contactTypeStats", contactTypeStats);
        model.addAttribute("contactTypes", contactTypes);
        model.addAttribute("currentContactType", contactType);
        model.addAttribute("currentIsCustomerContact", isCustomerContact);
        model.addAttribute("currentSearch", search);
        model.addAttribute("activeLink", "crm-contacts");

        return "crm/contact/list";
    }

    // ========================
    // VISTA DETALLE
    // ========================

    /**
     * Muestra el detalle de un contacto
     */
    @GetMapping("/{id}")
    public String viewContact(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Viewing contact detail for ID: {}", id);

        User user = getUserFromPrincipal(userDetails);
        Contact contact = getContactByIdAndCompany(id, user.getCompany());

        model.addAttribute("contact", contact);
        model.addAttribute("activeLink", "crm-contacts");

        return "crm/contact/detail";
    }

    // ========================
    // FORMULARIO NUEVO CONTACTO
    // ========================

    /**
     * Muestra el formulario para crear un nuevo contacto
     */
    @GetMapping("/new")
    @PreAuthorize("hasAuthority('CRM_CREATE')")
    public String newContactForm(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Displaying new contact form - customerId: {}, supplierId: {}", customerId, supplierId);

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();

        Contact contact = new Contact();

        // Si se especifica un customer o supplier, pre-asignarlo
        if (customerId != null) {
            Customer customer = getCustomerByIdAndCompany(customerId, company);
            contact.setCustomer(customer);
        } else if (supplierId != null) {
            Supplier supplier = getSupplierByIdAndCompany(supplierId, company);
            contact.setSupplier(supplier);
        }

        // Preparar datos para el formulario
        prepareContactFormModel(model, contact, company, false);

        return "crm/contact/form";
    }

    // ========================
    // FORMULARIO EDITAR CONTACTO
    // ========================

    /**
     * Muestra el formulario para editar un contacto existente
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('CRM_UPDATE')")
    public String editContactForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Displaying edit contact form for ID: {}", id);

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();
        Contact contact = getContactByIdAndCompany(id, company);

        prepareContactFormModel(model, contact, company, true);

        return "crm/contact/form";
    }

    // ========================
    // GUARDAR CONTACTO
    // ========================

    /**
     * Procesa el formulario de contacto (nuevo o edición)
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CRM_CREATE', 'CRM_UPDATE')")
    public String saveContact(
            @Valid @ModelAttribute("contact") Contact contact,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long supplierId,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Processing contact form submission for: {} {}",
                contact.getFirstName(), contact.getLastName());

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();
        boolean isEdit = contact.getId() != null;

        // Asignar customer o supplier basado en los parámetros
        if (customerId != null) {
            Customer customer = getCustomerByIdAndCompany(customerId, company);
            contact.setCustomer(customer);
            contact.setSupplier(null);
        } else if (supplierId != null) {
            Supplier supplier = getSupplierByIdAndCompany(supplierId, company);
            contact.setSupplier(supplier);
            contact.setCustomer(null);
        }

        // Validaciones adicionales
        if (result.hasErrors()) {
            logger.warn("Contact form validation errors: {}", result.getAllErrors());
            prepareContactFormModel(model, contact, company, isEdit);
            return "crm/contact/form";
        }

        try {
            // Validaciones de negocio personalizadas
            contactService.validateContact(contact);

            // Guardar contacto
            Contact savedContact = contactService.save(contact);

            // Mensaje de éxito
            String parentName = savedContact.isCustomerContact() ?
                    savedContact.getCustomer().getName() : savedContact.getSupplier().getName();
            String parentType = savedContact.isCustomerContact() ? "customer" : "supplier";

            String message = isEdit ?
                    String.format("Contact updated successfully: %s (%s: %s)",
                            savedContact.getFullName(), parentType, parentName) :
                    String.format("Contact created successfully: %s (%s: %s)",
                            savedContact.getFullName(), parentType, parentName);

            redirectAttributes.addFlashAttribute("successMessage", message);
            logger.info("Contact {} successfully: {}", isEdit ? "updated" : "created", savedContact.getFullName());

            return "redirect:/crm/contacts/" + savedContact.getId();

        } catch (IllegalArgumentException e) {
            logger.warn("Contact validation error: {}", e.getMessage());
            result.rejectValue("firstName", "validation.error", e.getMessage());

            prepareContactFormModel(model, contact, company, isEdit);
            return "crm/contact/form";

        } catch (Exception e) {
            logger.error("Error saving contact: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving contact: " + e.getMessage());
            return "redirect:/crm/contacts";
        }
    }

    // ========================
    // TRANSFERIR CONTACTO
    // ========================

    /**
     * Transfiere un contacto de customer a supplier o viceversa
     */
    @PostMapping("/{id}/transfer")
    @PreAuthorize("hasAuthority('CRM_UPDATE')")
    public String transferContact(
            @PathVariable Long id,
            @RequestParam Long targetId,
            @RequestParam String targetType,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Transferring contact {} to {} ID: {}", id, targetType, targetId);

        try {
            User user = getUserFromPrincipal(userDetails);
            Company company = user.getCompany();

            // Verificar que el contacto pertenece a la empresa del usuario
            Contact contact = getContactByIdAndCompany(id, company);

            Contact updatedContact;

            if ("customer".equals(targetType)) {
                Customer customer = getCustomerByIdAndCompany(targetId, company);
                updatedContact = contactService.transferContactToCustomer(id, customer);
            } else if ("supplier".equals(targetType)) {
                Supplier supplier = getSupplierByIdAndCompany(targetId, company);
                updatedContact = contactService.transferContactToSupplier(id, supplier);
            } else {
                throw new IllegalArgumentException("Invalid target type: " + targetType);
            }

            String message = String.format("Contact %s transferred successfully to %s: %s",
                    updatedContact.getFullName(),
                    targetType,
                    updatedContact.isCustomerContact() ?
                            updatedContact.getCustomer().getName() :
                            updatedContact.getSupplier().getName());

            redirectAttributes.addFlashAttribute("successMessage", message);
            logger.info("Contact transfer completed: {}", message);

        } catch (Exception e) {
            logger.error("Error transferring contact: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error transferring contact: " + e.getMessage());
        }

        return "redirect:/crm/contacts/" + id;
    }

    // ========================
    // ELIMINAR CONTACTO
    // ========================

    /**
     * Elimina un contacto
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('CRM_DELETE')")
    public String deleteContact(@PathVariable Long id,
                                RedirectAttributes redirectAttributes,
                                @AuthenticationPrincipal UserDetails userDetails) {

        logger.debug("Deleting contact with ID: {}", id);

        try {
            User user = getUserFromPrincipal(userDetails);

            // Verificar que el contacto pertenece a la empresa del usuario
            Contact contact = getContactByIdAndCompany(id, user.getCompany());

            String contactName = contact.getFullName();
            String parentName = contact.isCustomerContact() ?
                    contact.getCustomer().getName() : contact.getSupplier().getName();

            contactService.deleteById(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Contact deleted: %s (from %s)", contactName, parentName));
            logger.info("Contact deleted successfully: {} from {}", contactName, parentName);

        } catch (Exception e) {
            logger.error("Error deleting contact: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting contact: " + e.getMessage());
        }

        return "redirect:/crm/contacts";
    }

    // ========================
    // VISTAS ESPECÍFICAS
    // ========================

    /**
     * Muestra contactos de un cliente específico
     */
    @GetMapping("/customer/{customerId}")
    public String customerContacts(
            @PathVariable Long customerId,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUserFromPrincipal(userDetails);
        Customer customer = getCustomerByIdAndCompany(customerId, user.getCompany());

        Page<Contact> contacts = contactService.findByCustomer(customer, pageable);

        model.addAttribute("customer", customer);
        model.addAttribute("contacts", contacts);
        model.addAttribute("activeLink", "crm-contacts");

        return "crm/contact/customer-contacts";
    }

    /**
     * Muestra contactos de un proveedor específico
     */
    @GetMapping("/supplier/{supplierId}")
    public String supplierContacts(
            @PathVariable Long supplierId,
            @PageableDefault(size = 10, sort = "lastName") Pageable pageable,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getUserFromPrincipal(userDetails);
        Supplier supplier = getSupplierByIdAndCompany(supplierId, user.getCompany());

        Page<Contact> contacts = contactService.findBySupplier(supplier, pageable);

        model.addAttribute("supplier", supplier);
        model.addAttribute("contacts", contacts);
        model.addAttribute("activeLink", "crm-contacts");

        return "crm/contact/supplier-contacts";
    }

    /**
     * Muestra contactos principales (comerciales) de la empresa
     */
    @GetMapping("/primary")
    public String primaryContacts(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Displaying primary contacts");

        User user = getUserFromPrincipal(userDetails);
        Company company = user.getCompany();

        List<Contact> primaryContacts = contactService.findPrimaryContactsByCompany(company);

        model.addAttribute("primaryContacts", primaryContacts);
        model.addAttribute("activeLink", "crm-contacts");

        return "crm/contact/primary-contacts";
    }

    // ========================
    // API ENDPOINTS PARA AJAX
    // ========================

    /**
     * API endpoint para obtener customers activos (para formularios AJAX)
     */
    @GetMapping("/api/customers")
    @ResponseBody
    public List<Customer> getActiveCustomers(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromPrincipal(userDetails);
        return customerService.findActiveByCompany(user.getCompany());
    }

    /**
     * API endpoint para obtener suppliers activos (para formularios AJAX)
     */
    @GetMapping("/api/suppliers")
    @ResponseBody
    public List<Supplier> getActiveSuppliers(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserFromPrincipal(userDetails);
        return supplierService.findActiveByCompany(user.getCompany());
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
     * Obtiene un contacto por ID verificando que pertenece a la empresa del usuario
     */
    private Contact getContactByIdAndCompany(Long id, Company company) {
        Optional<Contact> contactOpt = contactService.findById(id);

        if (contactOpt.isEmpty()) {
            throw new IllegalArgumentException("Contact not found with ID: " + id);
        }

        Contact contact = contactOpt.get();

        // Verificar que pertenece a la empresa del usuario
        Company contactCompany = contact.isCustomerContact() ?
                contact.getCustomer().getCompany() : contact.getSupplier().getCompany();

        if (!contactCompany.getId().equals(company.getId())) {
            throw new IllegalArgumentException("Contact does not belong to your company");
        }

        return contact;
    }

    /**
     * Obtiene un customer por ID verificando que pertenece a la empresa del usuario
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

    /**
     * Obtiene un supplier por ID verificando que pertenece a la empresa del usuario
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

    /**
     * Prepara el modelo para el formulario de contacto
     */
    private void prepareContactFormModel(Model model, Contact contact, Company company, boolean isEdit) {
        // Obtener customers y suppliers activos para los dropdowns
        List<Customer> customers = customerService.findActiveByCompany(company);
        List<Supplier> suppliers = supplierService.findActiveByCompany(company);

        // Tipos de contacto
        List<Contact.ContactType> contactTypes = Arrays.asList(Contact.ContactType.values());

        model.addAttribute("contact", contact);
        model.addAttribute("customers", customers);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("contactTypes", contactTypes);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("activeLink", "crm-contacts");
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
        return "redirect:/crm/contacts";
    }
}