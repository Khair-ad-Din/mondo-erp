// src/main/java/com/mondo/erp/core/controller/RegistrationController.java
package com.mondo.erp.core.controller;

import com.mondo.erp.application.dto.RegistrationForm;
import com.mondo.erp.core.model.Company;
import com.mondo.erp.core.model.Role;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.service.CompanyService;
import com.mondo.erp.core.service.RoleService;
import com.mondo.erp.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final UserService userService;
    private final RoleService roleService;
    private final CompanyService companyService;

    @Autowired
    public RegistrationController(UserService userService, RoleService roleService, CompanyService companyService) {
        this.userService = userService;
        this.roleService = roleService;
        this.companyService = companyService;
    }

    @GetMapping
    public String registerForm(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }

        List<Company> companies = companyService.findAll();
        model.addAttribute("companies", companies);

        return "auth/register";
    }

    @PostMapping
    public String registerUser(
            @Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Custom validation
        if (!registrationForm.getPassword().equals(registrationForm.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "passwords.mismatch", "Passwords do not match");
        }

        // Check if username is already taken
        if (userService.existsByUsername(registrationForm.getUsername())) {
            result.rejectValue("username", "username.exists", "Username is already taken");
        }

        // Check if email is already registered
        if (userService.existsByEmail(registrationForm.getEmail())) {
            result.rejectValue("email", "email.exists", "Email is already registered");
        }

        if (result.hasErrors()) {
            // Re-load companies for the form
            model.addAttribute("companies", companyService.findAll());
            return "auth/register";
        }

        // Create new user
        User user = new User();
        user.setFirstname(registrationForm.getFirstname());
        user.setLastname(registrationForm.getLastname());
        user.setEmail(registrationForm.getEmail());
        user.setUsername(registrationForm.getUsername());
        user.setPassword(registrationForm.getPassword()); // UserService will encrypt
        user.setActive(true);

        // Set company
        Company company = companyService.findById(registrationForm.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid company ID"));
        user.setCompany(company);

        // Assign default USER role
        Role userRole = roleService.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default USER role not found"));
        user.setRoles(Collections.singleton(userRole));

        // Save user
        userService.save(user);

        redirectAttributes.addFlashAttribute("successMessage",
                "Registration successful! You can now log in with your new account.");
        return "redirect:/login";
    }
}