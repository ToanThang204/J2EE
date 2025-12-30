package com.hutech.demo.controller.view;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hutech.demo.model.Application;
import com.hutech.demo.model.Resume;
import com.hutech.demo.model.SavedJob;
import com.hutech.demo.service.ApplicationService;
import com.hutech.demo.service.ResumeService;
import com.hutech.demo.service.SavedJobService;
import com.hutech.demo.service.UserService;
import com.hutech.demo.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CandidateViewController {

    private final ResumeService resumeService;
    private final ApplicationService applicationService;
    private final SavedJobService savedJobService;
    private final UserService userService;

    @GetMapping("/candidate/profile")
    public String profile(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        com.hutech.demo.model.User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/profile";
    }

    @GetMapping("/candidate/profile/edit")
    public String editProfile(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        com.hutech.demo.model.User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("profile", user.getProfile());
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/profile-edit";
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

    @GetMapping("/candidate/cv-builder")
    public String cvBuilder(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/cv-builder";
    }

    @GetMapping({ "/candidate/applications", "/candidate/applied-jobs" })
    public String applications(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Application> applications = applicationService.getApplicationsByUser(userId);

        long total = applications.size();
        long pending = applications.stream()
                .filter(a -> a.getStatus() != null && "PENDING".equals(a.getStatus().name())).count();
        long reviewed = applications.stream()
                .filter(a -> a.getStatus() != null && "REVIEWED".equals(a.getStatus().name())).count();
        long matched = applications.stream().filter(a -> a.getStatus() != null
                && ("HIRED".equals(a.getStatus().name()) || "INTERVIEW".equals(a.getStatus().name()))).count();

        model.addAttribute("applications", applications);
        model.addAttribute("totalCount", total);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("reviewedCount", reviewed);
        model.addAttribute("matchedCount", matched);
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

    @GetMapping("/candidate/applications/{id}")
    public String applicationDetail(@PathVariable Long id, Model model) {
        Application application = applicationService.getApplicationById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        model.addAttribute("application", application);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/application-detail";
    }

    @GetMapping("/candidate/company-invites")
    public String companyInvites(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/company-invites";
    }

    @GetMapping("/candidate/cv-index")
    public String cvIndex(Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Resume> resumes = resumeService.getResumesByUser(userId);
        model.addAttribute("resumes", resumes);
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/cv-index";
    }

    @GetMapping("/candidate/messages")
    public String messages(Model model) {
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());
        return "candidate/messages";
    }
}
