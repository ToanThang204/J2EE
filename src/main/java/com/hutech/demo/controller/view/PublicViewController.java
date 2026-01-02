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
        try {
            // Lấy danh sách jobs và chuyển thành DTO để tránh lazy-loading issues
            List<Job> jobs = jobService.getAllJobs();
            List<com.hutech.demo.dto.JobDto> jobDtos = jobs.stream()
                .filter(job -> job.getStatus() == com.hutech.demo.model.enums.JobStatus.OPEN)
                .map(job -> {
                    com.hutech.demo.dto.JobDto dto = new com.hutech.demo.dto.JobDto();
                    dto.setId(job.getId());
                    dto.setTitle(job.getTitle());
                    dto.setDescription(job.getDescription());
                    dto.setSalaryRange(job.getSalaryRange());
                    dto.setLocation(job.getLocation());
                    dto.setEmploymentType(job.getEmploymentType());
                    dto.setExpirationDate(job.getExpirationDate());
                    dto.setStatus(job.getStatus());
                    
                    // Map company info nếu có
                    if (job.getCompany() != null) {
                        com.hutech.demo.dto.CompanyDto compDto = new com.hutech.demo.dto.CompanyDto();
                        compDto.setId(job.getCompany().getId());
                        compDto.setCompanyName(job.getCompany().getCompanyName());
                        compDto.setName(job.getCompany().getCompanyName());
                        compDto.setLogo(job.getCompany().getLogo());
                        dto.setCompany(compDto);
                    }
                    
                    return dto;
                })
                .toList();
            
            model.addAttribute("jobs", jobDtos);
            model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
            return "shared/job-list";
        } catch (Exception ex) {
            System.err.println("Error loading job list: " + ex.getMessage());
            ex.printStackTrace();
            model.addAttribute("jobs", new java.util.ArrayList<>());
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải danh sách việc làm");
            model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
            return "shared/job-list";
        }
    }

    @GetMapping("/jobs/{id}")
    public String jobDetail(@PathVariable Long id, Model model) {
        try {
            Job job = jobService.getJobById(id)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            // Map to DTO to avoid lazy-loading / serialization issues during template rendering
            com.hutech.demo.dto.JobDto jobDto = new com.hutech.demo.dto.JobDto();
            jobDto.setId(job.getId());
            jobDto.setTitle(job.getTitle());
            jobDto.setDescription(job.getDescription());
            jobDto.setRequirements(job.getRequirements());
            jobDto.setSalaryRange(job.getSalaryRange());
            jobDto.setLocation(job.getLocation());
            jobDto.setEmploymentType(job.getEmploymentType());
            jobDto.setPostedDate(job.getPostedDate());
            jobDto.setExpirationDate(job.getExpirationDate());
            jobDto.setStatus(job.getStatus());
            jobDto.setCreatedAt(job.getCreatedAt());
            jobDto.setUpdatedAt(job.getUpdatedAt());

            if (job.getCompany() != null) {
                com.hutech.demo.dto.CompanyDto comp = new com.hutech.demo.dto.CompanyDto();
                comp.setId(job.getCompany().getId());
                comp.setCompanyName(job.getCompany().getCompanyName());
                // also set 'name' for templates that use company.name
                comp.setName(job.getCompany().getCompanyName());
                comp.setLogo(job.getCompany().getLogo());
                comp.setAddress(job.getCompany().getAddress());
                jobDto.setCompany(comp);
            }

            model.addAttribute("job", jobDto);
            model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
            return "shared/job-detail";
        } catch (Exception ex) {
            // Log and show friendly error page so response is complete instead of truncated
            System.err.println("Error rendering job detail for id=" + id + ": " + ex.getMessage());
            ex.printStackTrace();
            model.addAttribute("errorMessage", "Không thể hiển thị chi tiết công việc. Vui lòng thử lại sau.");
            model.addAttribute("jobId", id);
            return "shared/job-detail-error";
        }
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
        try {
            Company company = companyService.getCompanyById(id)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
            
            // Convert to DTO to avoid lazy-loading issues
            com.hutech.demo.dto.CompanyDto companyDto = new com.hutech.demo.dto.CompanyDto();
            companyDto.setId(company.getId());
            companyDto.setCompanyName(company.getCompanyName());
            companyDto.setName(company.getCompanyName()); // alias for template compatibility
            companyDto.setDescription(company.getDescription());
            companyDto.setLogo(company.getLogo());
            companyDto.setAddress(company.getAddress());
            companyDto.setWebsite(company.getWebsite());
            // Industry and employeeCount fields don't exist in Company entity
            companyDto.setIndustry(null);
            companyDto.setEmployeeCount(null);
            
            // Get jobs and convert to DTOs
            List<Job> companyJobs = jobService.getJobsByCompany(id);
            List<com.hutech.demo.dto.JobDto> jobDtos = companyJobs.stream()
                .filter(job -> job.getStatus() == com.hutech.demo.model.enums.JobStatus.OPEN)
                .map(job -> {
                    com.hutech.demo.dto.JobDto dto = new com.hutech.demo.dto.JobDto();
                    dto.setId(job.getId());
                    dto.setTitle(job.getTitle());
                    dto.setDescription(job.getDescription());
                    dto.setSalaryRange(job.getSalaryRange());
                    dto.setLocation(job.getLocation());
                    dto.setEmploymentType(job.getEmploymentType());
                    dto.setExpirationDate(job.getExpirationDate());
                    dto.setStatus(job.getStatus());
                    
                    // Set company info for job card
                    if (job.getCompany() != null) {
                        com.hutech.demo.dto.CompanyDto jobCompDto = new com.hutech.demo.dto.CompanyDto();
                        jobCompDto.setId(job.getCompany().getId());
                        jobCompDto.setCompanyName(job.getCompany().getCompanyName());
                        jobCompDto.setName(job.getCompany().getCompanyName());
                        jobCompDto.setLogo(job.getCompany().getLogo());
                        dto.setCompany(jobCompDto);
                    }
                    
                    return dto;
                })
                .toList();
            
            model.addAttribute("company", companyDto);
            model.addAttribute("jobs", jobDtos);
            model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
            return "shared/company-detail";
        } catch (Exception ex) {
            System.err.println("Error loading company detail for id=" + id + ": " + ex.getMessage());
            ex.printStackTrace();
            model.addAttribute("errorMessage", "Không thể hiển thị thông tin công ty. Vui lòng thử lại sau.");
            model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
            return "shared/company-detail";
        }
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "shared/notifications";
    }
}
