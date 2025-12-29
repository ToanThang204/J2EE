package com.hutech.demo.controller.view;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.Job;
import com.hutech.demo.model.Application;
import com.hutech.demo.service.CompanyService;
import com.hutech.demo.service.JobService;
import com.hutech.demo.service.ApplicationService;
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
        Long userId = SecurityUtils.getCurrentUserId();
        List<Company> companies = companyService.getAllCompanies().stream()
                .filter(c -> c.getUserId().equals(userId))
                .toList();
        
        List<Job> allJobs = jobService.getAllJobs();
        List<Job> userJobs = allJobs.stream()
                .filter(job -> companies.stream()
                        .anyMatch(c -> c.getId().equals(job.getCompany().getId())))
                .toList();
        
        model.addAttribute("jobs", userJobs);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "employer/job-list";
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
    public String applications(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Company> companies = companyService.getAllCompanies().stream()
                .filter(c -> c.getUserId().equals(userId))
                .toList();
        
        List<Application> allApplications = applicationService.getAllApplications();
        List<Application> userApplications = allApplications.stream()
                .filter(app -> companies.stream()
                        .anyMatch(c -> c.getId().equals(app.getJob().getCompany().getId())))
                .toList();
        
        model.addAttribute("applications", userApplications);
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

