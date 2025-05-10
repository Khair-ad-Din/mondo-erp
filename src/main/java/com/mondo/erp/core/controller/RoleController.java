// src/main/java/com/mondo/erp/core/controller/RoleController.java
package com.mondo.erp.core.controller;

import com.mondo.erp.core.model.Permission;
import com.mondo.erp.core.model.Role;
import com.mondo.erp.core.service.PermissionService;
import com.mondo.erp.core.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    @Autowired
    public RoleController(RoleService roleService, PermissionService permissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public String listRoles(Model model) {
        List<Role> roles = roleService.findAll();
        model.addAttribute("roles", roles);
        return "admin/role/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public String newRoleForm(Model model) {
        prepareFormModel(model, new Role());
        return "admin/role/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public String editRoleForm(@PathVariable Long id, Model model) {
        Role role = roleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID: " + id));
        prepareFormModel(model, role);
        return "admin/role/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CREATE', 'ROLE_UPDATE')")
    public String saveRole(@Valid @ModelAttribute("role") Role role,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        // Validate unique name
        boolean isNew = (role.getId() == null);
        if (isNew && roleService.existsByName(role.getName())) {
            result.rejectValue("name", "duplicate", "Role name must be unique");
        }

        if (result.hasErrors()) {
            prepareFormModel(model, role);
            return "admin/role/form";
        }

        roleService.save(role);
        redirectAttributes.addFlashAttribute("successMessage",
                (isNew ? "Role created" : "Role updated") + " successfully");
        return "redirect:/admin/roles";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public String deleteRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roleService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Role deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete role. It may be assigned to users.");
        }
        return "redirect:/admin/roles";
    }

    private void prepareFormModel(Model model, Role role) {
        List<Permission> permissions = permissionService.findAll();
        Set<String> modules = permissionService.findAllModules();

        model.addAttribute("role", role);
        model.addAttribute("permissions", permissions);
        model.addAttribute("modules", modules);
    }
}