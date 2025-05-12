// src/main/java/com/mondo/erp/core/controller/UserController.java
package com.mondo.erp.core.controller;

import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.Role;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.service.CompanyService;
import com.mondo.erp.core.service.RoleService;
import com.mondo.erp.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(
            UserService userService,
            RoleService roleService,
            CompanyService companyService,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.companyService = companyService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public String listUsers(
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long roleId) {

        // Get all users
        List<User> allUsers = userService.findAll();

        // Apply filters if provided
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> {
                    // Filter by search term
                    if (search != null && !search.isEmpty()) {
                        String searchLower = search.toLowerCase();
                        boolean matches = user.getUsername().toLowerCase().contains(searchLower) ||
                                user.getFirstname().toLowerCase().contains(searchLower) ||
                                user.getLastname().toLowerCase().contains(searchLower) ||
                                user.getEmail().toLowerCase().contains(searchLower);
                        if (!matches) {
                            return false;
                        }
                    }

                    // Filter by company
                    if (companyId != null) {
                        if (user.getCompany() == null || !user.getCompany().getId().equals(companyId)) {
                            return false;
                        }
                    }

                    // Filter by role
                    if (roleId != null) {
                        boolean hasRole = user.getRoles().stream()
                                .anyMatch(role -> role.getId().equals(roleId));
                        if (!hasRole) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // Get companies and roles for filter dropdowns
        List<Company> companies = companyService.findAll();
        List<Role> roles = roleService.findAll();

        model.addAttribute("users", filteredUsers);
        model.addAttribute("companies", companies);
        model.addAttribute("roles", roles);
        return "admin/user/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public String newUserForm(Model model) {
        User user = new User();
        user.setActive(true);
        prepareFormModel(model, user);
        return "admin/user/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        prepareFormModel(model, user);
        return "admin/user/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'USER_UPDATE')")
    public String saveUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam(value = "password", required = false) String password,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean isNew = (user.getId() == null);

        // Check username uniqueness for new users
        if (isNew && userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "duplicate", "Username is already taken");
        }

        // Check email uniqueness
        User existingUser = userService.findByEmail(user.getEmail()).orElse(null);
        if (existingUser != null && (isNew || !existingUser.getId().equals(user.getId()))) {
            result.rejectValue("email", "duplicate", "Email is already registered");
        }

        // Validate password for new users
        if (isNew && (password == null || password.isEmpty())) {
            result.rejectValue("password", "required", "Password is required");
        }

        // Check if at least one role is selected
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            result.rejectValue("roles", "required", "At least one role must be selected");
        }

        if (result.hasErrors()) {
            prepareFormModel(model, user);
            return "admin/user/form";
        }

        // Set password for new users
        if (isNew) {
            user.setPassword(password);
        }

        userService.save(user);

        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "User created successfully" : "User updated successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public String activateUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));

        user.setActive(true);
        userService.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "User activated successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public String deactivateUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));

        user.setActive(false);
        userService.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "User deactivated successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public String resetPassword(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));

        // Set a default password (you might want to make this configurable or random)
        String defaultPassword = "Password123";
        user.setPassword(defaultPassword);
        userService.save(user);

        redirectAttributes.addFlashAttribute("successMessage",
                "Password for " + user.getUsername() + " has been reset to: " + defaultPassword);
        return "redirect:/admin/users";
    }

    private void prepareFormModel(Model model, User user) {
        List<Company> companies = companyService.findAll();
        List<Role> roles = roleService.findAll();

        model.addAttribute("user", user);
        model.addAttribute("companies", companies);
        model.addAttribute("roles", roles);
    }
}