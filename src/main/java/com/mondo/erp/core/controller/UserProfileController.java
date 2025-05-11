// src/main/java/com/mondo/erp/core/controller/UserProfileController.java
package com.mondo.erp.core.controller;

import com.mondo.erp.core.model.User;
import com.mondo.erp.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        // Verify this is the current user
        if (!user.getUsername().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only update your own profile");
            return "redirect:/user/profile";
        }

        // Check if email is already used by another user
        User existingUser = userService.findByEmail(user.getEmail()).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            result.rejectValue("email", "duplicate", "Email is already in use");
        }

        if (result.hasErrors()) {
            return "user/profile";
        }

        // Get original user to preserve fields that shouldn't be updated
        User originalUser = userService.findById(user.getId()).orElseThrow();

        // Update only allowed fields
        originalUser.setFirstname(user.getFirstname());
        originalUser.setLastname(user.getLastname());
        originalUser.setEmail(user.getEmail());

        userService.save(originalUser);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        return "redirect:/user/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        // Verify passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
            return "redirect:/user/change-password";
        }

        // Get current user
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect");
            return "redirect:/user/change-password";
        }

        // Update password
        user.setPassword(newPassword); // The UserService will encode it
        userService.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
        return "redirect:/user/profile";
    }
}