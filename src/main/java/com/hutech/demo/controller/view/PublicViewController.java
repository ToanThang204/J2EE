package com.hutech.demo.controller.view;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.Job;
import com.hutech.demo.service.CompanyService;
import com.hutech.demo.service.JobService;
import com.hutech.demo.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PublicViewController {

    private final JobService jobService;
    private final CompanyService companyService;

    @GetMapping("/")
    public String index(Model model) {
        // Lấy các jobs mới nhất có status OPEN
        List<Job> recentJobs = jobService.getAllJobs().stream()
                .filter(job -> job.getStatus() == com.hutech.demo.model.enums.JobStatus.OPEN)
                .sorted((j1, j2) -> {
                    if (j2.getCreatedAt() == null)
                        return -1;
                    if (j1.getCreatedAt() == null)
                        return 1;
                    return j2.getCreatedAt().compareTo(j1.getCreatedAt());
                })
                .limit(6)
                .toList();

        model.addAttribute("recentJobs", recentJobs);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "shared/index";
    }

    @GetMapping("/jobs")
    public String jobList(Model model) {
        List<Job> jobs = jobService.getAllJobs();
        model.addAttribute("jobs", jobs);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "shared/job-list";
    }

    @GetMapping("/jobs/{id}")
    public String jobDetail(@PathVariable Long id, Model model) {
        Job job = jobService.getJobById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        model.addAttribute("job", job);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "shared/job-detail";
    }

    @GetMapping("/companies")
    public String companiesList(Model model) {
        List<Company> companies = companyService.getAllCompanies();
        model.addAttribute("companies", companies);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "shared/companies-list";
    }

    @GetMapping("/companies/{id}")
    public String companyDetail(@PathVariable Long id, Model model) {
        Company company = companyService.getCompanyById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        List<Job> companyJobs = jobService.getJobsByCompany(id);
        model.addAttribute("company", company);
        model.addAttribute("jobs", companyJobs);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "shared/company-detail";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "shared/notifications";
    }
}
