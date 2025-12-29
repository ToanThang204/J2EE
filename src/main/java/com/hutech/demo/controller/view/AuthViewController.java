package com.hutech.demo.controller.view;

import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    @GetMapping("/login")
    public String login(Model model) {
        // If already authenticated, redirect to homepage
        if (SecurityUtils.getCurrentUser() != null) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (SecurityUtils.getCurrentUser() != null) {
            return "redirect:/";
        }
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "auth/reset-password";
    }
}

