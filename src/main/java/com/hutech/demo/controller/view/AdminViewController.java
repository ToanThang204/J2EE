package com.hutech.demo.controller.view;

import com.hutech.demo.model.User;
import com.hutech.demo.model.Category;
import com.hutech.demo.model.Company;
import com.hutech.demo.model.enums.CompanyStatus;
import com.hutech.demo.model.Payment;
import com.hutech.demo.repository.PaymentRepository;
import com.hutech.demo.service.UserService;
import com.hutech.demo.service.CategoryService;
import com.hutech.demo.service.CompanyService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminViewController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final CompanyService companyService;
    private final PaymentRepository paymentRepository;

    @GetMapping("/admin")
    public String adminIndex() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<User> allUsers = userService.getAllUsers();
        List<Company> allCompanies = companyService.getAllCompanies();

        long pendingCompanies = allCompanies.stream()
                .filter(c -> c.getStatus() == CompanyStatus.PENDING)
                .count();

        long approvedCompanies = allCompanies.stream()
                .filter(c -> c.getStatus() == CompanyStatus.ACTIVE)
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

    @GetMapping("/admin/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/user-view";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/user-edit";
    }

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/categories";
    }

    @GetMapping("/admin/companies-pending")
    public String companiesPending(Model model) {
        List<Company> pendingCompanies = companyService.getCompaniesByStatus(CompanyStatus.PENDING);
        model.addAttribute("pendingCompanies", pendingCompanies);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/companies-pending";
    }

    @GetMapping("/admin/companies")
    public String companies(Model model) {
        List<Company> companies = companyService.getAllCompanies();

        // Calculate stats in controller to avoid Thymeleaf stream issues
        long pendingCount = companies.stream()
                .filter(c -> c.getStatus() == CompanyStatus.PENDING)
                .count();
        long activeCount = companies.stream()
                .filter(c -> c.getStatus() == CompanyStatus.ACTIVE)
                .count();
        long rejectedCount = companies.stream()
                .filter(c -> c.getStatus() == CompanyStatus.REJECTED)
                .count();

        model.addAttribute("companies", companies);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/companies";
    }

    @GetMapping("/admin/revenue-report")
    public String revenueReport(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/revenue-report";
    }

    @GetMapping("/admin/payments-pending")
    public String paymentsPending(Model model) {
        List<Payment> pendingPayments = paymentRepository.findByStatus("pending");
        model.addAttribute("pendingPayments", pendingPayments);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "admin/payments-pending";
    }
}
