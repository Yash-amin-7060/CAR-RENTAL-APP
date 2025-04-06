package com.example.carrental.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.carrental.model.User;
import com.example.carrental.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, BindingResult result, Model model) {
        try {
            // Validate required fields
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                model.addAttribute("error", "First name is required");
                return "register";
            }
            
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                model.addAttribute("error", "Last name is required");
                return "register";
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                model.addAttribute("error", "Email is required");
                return "register";
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                model.addAttribute("error", "Password is required");
                return "register";
            }
            
            if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
                model.addAttribute("error", "Phone number is required");
                return "register";
            }
            
            // Validate phone number format
            if (!user.getPhoneNumber().matches("[0-9]{10}")) {
                model.addAttribute("error", "Phone number must be 10 digits");
                return "register";
            }
            
            // Trim all string fields
            user.setFirstName(user.getFirstName().trim());
            user.setLastName(user.getLastName().trim());
            user.setEmail(user.getEmail().trim());
            user.setPhoneNumber(user.getPhoneNumber().trim());
            
            // Register the user
            userService.registerUser(user);
            
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "register";
        }
    }
} 