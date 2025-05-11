// src/main/java/com/mondo/erp/core/controller/PasswordResetController.java
package com.mondo.erp.core.controller;

import com.mondo.erp.core.model.PasswordResetToken;
import com.mondo.erp.core.model.User;
import com.mondo.erp.core.service.PasswordResetService;
import com.mondo.erp.core.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PasswordResetController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @Autowired
    public PasswordResetController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Optional<User> user = userService.findByEmail(email);

        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "We couldn't find an account with that email address.");
            return "redirect:/forgot-password";
        }

        // Create token
        PasswordResetToken token = passwordResetService.createToken(user.get());

        // Build reset URL
        String appUrl = request.getScheme() + "://" + request.getServerName();

        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            appUrl += ":" + request.getServerPort();
        }

        appUrl += request.getContextPath() + "/reset-password?token=" + token.getToken();

        // Send email
        boolean emailSent = passwordResetService.sendPasswordResetEmail(user.get(), appUrl);

        if (emailSent) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "A password reset link has been sent to your email address.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "There was a problem sending the password reset email. Please try again later.");
        }

        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        Optional<PasswordResetToken> resetToken = passwordResetService.getValidToken(token);

        model.addAttribute("token", token);
        model.addAttribute("validToken", resetToken.isPresent());

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            redirectAttributes.addFlashAttribute("token", token);
            redirectAttributes.addFlashAttribute("validToken", true);
            return "redirect:/reset-password?token=" + token;
        }

        // Get token
        Optional<PasswordResetToken> resetToken = passwordResetService.getValidToken(token);

        if (resetToken.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The password reset link is invalid or has expired.");
            return "redirect:/forgot-password";
        }

        // Reset password
        User user = resetToken.get().getUser();
        boolean success = passwordResetService.resetPassword(user, password);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your password has been reset successfully. You can now log in with your new password.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "There was a problem resetting your password. Please try again.");
        }

        return "redirect:/login";
    }
}