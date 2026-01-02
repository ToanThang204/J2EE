package com.hutech.demo.controller;

import com.hutech.demo.model.Application;
import com.hutech.demo.model.enums.ApplicationStatus;
import com.hutech.demo.service.ApplicationService;
import com.hutech.demo.model.Job;
import com.hutech.demo.model.Resume;
import com.hutech.demo.repository.JobRepository;
import com.hutech.demo.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        Application application = applicationService.getApplicationById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Long applicationUserId = application.getUserId();
        Long jobOwnerId = application.getJob().getCompany().getUserId();
        
        if (!SecurityUtils.isAdmin() && !currentUserId.equals(applicationUserId) && !currentUserId.equals(jobOwnerId)) {
            throw new AccessDeniedException("Bạn không có quyền xem application này");
        }
        
        return ResponseEntity.ok(application);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Application>> getApplicationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(applicationService.getApplicationsByUser(userId));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getApplicationsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Application>> getApplicationsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(applicationService.getApplicationsByCompany(companyId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Application>> getApplicationsByStatus(@PathVariable ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.getApplicationsByStatus(status));
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<?> createApplication(@RequestBody Application application,
                                                         @RequestParam(required = false) Long jobId,
                                                         @RequestParam(required = false) Long resumeId) {
        log.debug("Incoming application payload: {}", application);

        // Ensure job is a managed reference so Hibernate can set FK correctly
        try {
            if (application.getJob() != null && application.getJob().getId() != null) {
                Job managed = jobRepository.getReferenceById(application.getJob().getId());
                application.setJob(managed);
            } else if (application.getJob() == null && jobId != null) {
                Job managed = jobRepository.getReferenceById(jobId);
                application.setJob(managed);
            }

            if (application.getResume() != null && application.getResume().getId() != null) {
                Resume managedR = resumeRepository.getReferenceById(application.getResume().getId());
                application.setResume(managedR);
            } else if (application.getResume() == null && resumeId != null) {
                Resume managedR = resumeRepository.getReferenceById(resumeId);
                application.setResume(managedR);
            }
        } catch (Exception ex) {
            log.warn("Could not resolve job/resume references: {}", ex.getMessage());
        }

        application.setUserId(SecurityUtils.getCurrentUserId());

        Long jobIdLog = application.getJob() != null ? application.getJob().getId() : null;
        Long resumeIdLog = application.getResume() != null ? application.getResume().getId() : null;
        log.debug("Creating application - userId={}, jobId={}, resumeId={}, coverLetterPresent={}",
                SecurityUtils.getCurrentUserId(), jobIdLog, resumeIdLog, application.getCoverLetter() != null);

        try {
            Application createdApplication = applicationService.createApplication(application);
            // Return minimal envelope {success:true, data:{...}} to match frontend expectation
            Map<String, Object> resp = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("id", createdApplication.getId());
            data.put("status", createdApplication.getStatus());
            resp.put("success", true);
            resp.put("data", data);
            log.debug("Application created with id={}", createdApplication.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception ex) {
            log.error("Failed to create application", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create application", "detail", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Application> updateApplicationStatus(@PathVariable Long id, @RequestParam ApplicationStatus status) {
        Application application = applicationService.getApplicationById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        Long jobOwnerId = application.getJob().getCompany().getUserId();
        if (!SecurityUtils.isAdmin() && !SecurityUtils.getCurrentUserId().equals(jobOwnerId)) {
            throw new AccessDeniedException("Bạn không có quyền cập nhật application này");
        }
        
        Application updatedApplication = applicationService.updateApplicationStatus(id, status);
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
