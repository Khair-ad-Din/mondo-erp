package com.mondo.erp.core.controller;

import com.mondo.erp.core.model.User;
import com.mondo.erp.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public String listUser(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "core/user/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        return "core/user/form";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        return "core/user/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'USER_UPDATE')")
    public String saveUser (@Valid @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "core/user/form";
        }

        userService.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }
}
