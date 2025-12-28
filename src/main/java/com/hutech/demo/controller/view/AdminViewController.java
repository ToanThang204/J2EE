package com.hutech.demo.controller.view;

import com.hutech.demo.model.User;
import com.hutech.demo.model.Category;
import com.hutech.demo.model.Company;
import com.hutech.demo.service.UserService;
import com.hutech.demo.service.CategoryService;
import com.hutech.demo.service.CompanyService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final CompanyService companyService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<User> allUsers = userService.getAllUsers();
        List<Company> allCompanies = companyService.getAllCompanies();
        
        long pendingCompanies = allCompanies.stream()
                .filter(c -> c.getStatus() != null && c.getStatus().toString().equals("PENDING"))
                .count();
        
        long approvedCompanies = allCompanies.stream()
                .filter(c -> c.getStatus() != null && c.getStatus().toString().equals("APPROVED"))
                .count();
        
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalCompanies", allCompanies.size());
        model.addAttribute("pendingCompanies", pendingCompanies);
        model.addAttribute("approvedCompanies", approvedCompanies);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/users";
    }

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/categories";
    }
}

