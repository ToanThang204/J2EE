package com.hutech.demo.controller.view;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.Job;
import com.hutech.demo.model.Application;
import com.hutech.demo.service.CompanyService;
import com.hutech.demo.service.JobService;
import com.hutech.demo.service.ApplicationService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EmployerViewController {

    private final CompanyService companyService;
    private final JobService jobService;
    private final ApplicationService applicationService;

    @GetMapping("/employer/dashboard")
    public String dashboard(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Company> companies = companyService.getAllCompanies();
        // Filter companies by user
        companies = companies.stream()
                .filter(c -> c.getUserId().equals(userId))
                .toList();
        
        model.addAttribute("companies", companies);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/dashboard";
    }

    @GetMapping("/employer")
    public String employerRoot() {
        return "redirect:/employer/dashboard";
    }

    @GetMapping("/employer/company/info")
    public String companyInfo(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Company> companies = companyService.getAllCompanies();
        Company company = companies.stream()
                .filter(c -> c.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        
        model.addAttribute("company", company);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/company-info";
    }

    @GetMapping("/employer/profile")
    public String profile(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/profile";
    }

    @GetMapping("/employer/jobs")
    public String jobList(Model model) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                model.addAttribute("jobs", new java.util.ArrayList<>());
                model.addAttribute("currentUser", null);
                return "employer/job-list";
            }
            
            // Get companies by user ID
            List<Company> companies = companyService.getCompaniesByUserId(userId);
            
            // Collect all jobs from user's companies
            List<Job> userJobs = new java.util.ArrayList<>();
            if (companies != null && !companies.isEmpty()) {
                for (Company company : companies) {
                    if (company != null && company.getId() != null) {
                        try {
                            List<Job> companyJobs = jobService.getJobsByCompany(company.getId());
                            if (companyJobs != null && !companyJobs.isEmpty()) {
                                userJobs.addAll(companyJobs);
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading jobs for company " + company.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            // Debug logging
            System.out.println("=== Job List Debug ===");
            System.out.println("User ID: " + userId);
            System.out.println("Companies found: " + (companies != null ? companies.size() : 0));
            System.out.println("Total jobs found: " + userJobs.size());
            
            model.addAttribute("jobs", userJobs);
            model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
            return "employer/job-list";
        } catch (Exception e) {
            System.err.println("=== ERROR in jobList ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            model.addAttribute("jobs", new java.util.ArrayList<>());
            try {
                model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
            } catch (Exception ex) {
                model.addAttribute("currentUser", null);
            }
            return "employer/job-list";
        }
    }

    @GetMapping("/employer/jobs/create")
    public String jobCreate(Model model, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // Tìm công ty theo userId trước
        List<Company> userCompanies = companyService.getCompaniesByUserId(userId);
        Company userCompany = userCompanies.isEmpty() ? null : userCompanies.get(0);
        
        // Nếu không tìm thấy, lấy công ty ACTIVE đầu tiên (cho trường hợp user_id chưa được gán)
        if (userCompany == null) {
            List<Company> activeCompanies = companyService.getCompaniesByStatus(com.hutech.demo.model.enums.CompanyStatus.ACTIVE);
            userCompany = activeCompanies.isEmpty() ? null : activeCompanies.get(0);
        }
        
        if (userCompany == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa có công ty. Vui lòng tạo công ty trước khi đăng tin tuyển dụng.");
            return "redirect:/employer/company/info";
        }
        
        model.addAttribute("company", userCompany);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/job-create";
    }

    @GetMapping("/employer/jobs/{id}/edit")
    public String jobEdit(@PathVariable Long id, Model model, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Job job = jobService.getJobById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        Long userId = SecurityUtils.getCurrentUserId();
        
        // Tìm công ty theo userId trước
        List<Company> userCompanies = companyService.getCompaniesByUserId(userId);
        Company userCompany = userCompanies.isEmpty() ? null : userCompanies.get(0);
        
        // Nếu không tìm thấy, lấy công ty ACTIVE đầu tiên
        if (userCompany == null) {
            List<Company> activeCompanies = companyService.getCompaniesByStatus(com.hutech.demo.model.enums.CompanyStatus.ACTIVE);
            userCompany = activeCompanies.isEmpty() ? null : activeCompanies.get(0);
        }
        
        if (userCompany == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa có công ty.");
            return "redirect:/employer/company/info";
        }
        
        model.addAttribute("job", job);
        model.addAttribute("company", userCompany);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/job-edit";
    }

    @GetMapping("/employer/applications")
    public String applicationsPage(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        // Provide companies list (or empty) so frontend knows company IDs
        List<Company> companies = userId == null ? java.util.List.of() : companyService.getCompaniesByUserId(userId);
        model.addAttribute("companies", companies);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/applications";
    }

    @GetMapping("/employer/companies/{id}/members")
    public String companyMembers(@PathVariable Long id, Model model) {
        Company company = companyService.getCompanyById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        model.addAttribute("company", company);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/company-members";
    }

    @GetMapping("/employer/payment")
    public String payment(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/payment";
    }

    @GetMapping("/employer/upgrade")
    public String upgrade(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/upgrade";
    }
}

