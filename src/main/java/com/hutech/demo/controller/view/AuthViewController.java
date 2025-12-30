package com.hutech.demo.controller.view;

import com.hutech.demo.util.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthViewController {

    @GetMapping("/login")
    public String login(HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        model.addAttribute("currentUser", null);
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(HttpServletResponse response, Model model) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        model.addAttribute("currentUser", null);
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

    @GetMapping("/auth/oauth-callback")
    public String oauthCallback(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "auth/oauth-callback";
    }
}
