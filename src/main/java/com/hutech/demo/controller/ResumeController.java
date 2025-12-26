package com.hutech.demo.controller;

import com.hutech.demo.model.Resume;
import com.hutech.demo.service.ResumeService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Resume>> getAllResumes() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResumeById(@PathVariable Long id) {
        Resume resume = resumeService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        if (!SecurityUtils.isAdmin() && !resume.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn không có quyền xem resume này");
        }
        
        return ResponseEntity.ok(resume);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Resume>> getResumesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(resumeService.getResumesByUser(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Resume> createResume(@RequestBody Resume resume) {
        resume.setUserId(SecurityUtils.getCurrentUserId());
        Resume createdResume = resumeService.createResume(resume);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdResume);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<Resume> updateResume(@PathVariable Long id, @RequestBody Resume resume) {
        Resume existingResume = resumeService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        if (!SecurityUtils.isAdmin() && !existingResume.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn không có quyền sửa resume này");
        }
        
        Resume updatedResume = resumeService.updateResume(id, resume);
        return ResponseEntity.ok(updatedResume);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<Void> deleteResume(@PathVariable Long id) {
        Resume existingResume = resumeService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        if (!SecurityUtils.isAdmin() && !existingResume.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa resume này");
        }
        
        resumeService.deleteResume(id);
        return ResponseEntity.noContent().build();
    }
}
