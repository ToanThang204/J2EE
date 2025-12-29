package com.hutech.demo.controller.view;

import com.hutech.demo.model.Resume;
import com.hutech.demo.model.Application;
import com.hutech.demo.model.SavedJob;
import com.hutech.demo.service.ResumeService;
import com.hutech.demo.service.ApplicationService;
import com.hutech.demo.service.SavedJobService;
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
public class CandidateViewController {

    private final ResumeService resumeService;
    private final ApplicationService applicationService;
    private final SavedJobService savedJobService;

    @GetMapping("/candidate/dashboard")
    public String dashboard(Model model) {
        // Authentication will be handled by JavaScript on page load
        return "candidate/dashboard";
    }

    @GetMapping("/candidate/dashboard-old")
    public String dashboardOld(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Application> applications = applicationService.getApplicationsByUser(userId);
        List<SavedJob> savedJobs = savedJobService.getSavedJobsByUser(userId);
        
        model.addAttribute("applications", applications);
        model.addAttribute("savedJobs", savedJobs);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/dashboard";
    }

    @GetMapping("/candidate/profile")
    public String profile(Model model) {
        // Authentication will be handled by JavaScript on page load
        return "candidate/profile";
    }

    @GetMapping("/candidate/resumes")
    public String resumeList(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Resume> resumes = resumeService.getResumesByUser(userId);
        model.addAttribute("resumes", resumes);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/resume-list";
    }

    @GetMapping("/candidate/resumes/create")
    public String resumeCreate(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/resume-create";
    }

    @GetMapping("/candidate/resumes/{id}/edit")
    public String resumeEdit(@PathVariable Long id, Model model) {
        Resume resume = resumeService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        model.addAttribute("resume", resume);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/resume-edit";
    }

    @GetMapping("/candidate/applications")
    public String applications(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Application> applications = applicationService.getApplicationsByUser(userId);
        model.addAttribute("applications", applications);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/applications";
    }

    @GetMapping("/candidate/saved-jobs")
    public String savedJobs(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<SavedJob> savedJobs = savedJobService.getSavedJobsByUser(userId);
        model.addAttribute("savedJobs", savedJobs);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/saved-jobs";
    }

    @GetMapping("/candidate/company-invites")
    public String companyInvites(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/company-invites";
    }
}

