package com.hutech.demo.controller;

import com.hutech.demo.dto.ResumeDto;
import com.hutech.demo.model.Resume;
import com.hutech.demo.service.ResumeService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResumeDto>> getAllResumes() {
        List<ResumeDto> list = resumeService.getAllResumes().stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDto> getResumeById(@PathVariable Long id) {
        Resume resume = resumeService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!SecurityUtils.isAdmin() && !resume.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn không có quyền xem resume này");
        }

        return ResponseEntity.ok(toDto(resume));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResumeDto>> getResumesByUser(@PathVariable Long userId) {
        List<ResumeDto> list = resumeService.getResumesByUser(userId).stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ResumeDto> createResume(@RequestBody ResumeDto dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Creating resume for user: {}", currentUserId);
        log.debug("Resume DTO: {}", dto);
        
        if (currentUserId == null) {
            log.error("Cannot get current user ID from security context!");
            throw new RuntimeException("User authentication required");
        }
        
        try {
            Resume resume = new Resume();
            resume.setTitle(dto.getTitle());
            resume.setFullName(dto.getFullName());
            resume.setEmail(dto.getEmail());
            resume.setPhone(dto.getPhone());
            resume.setAddress(dto.getAddress());
            resume.setSummary(dto.getSummary());
            resume.setPhoto(dto.getPhoto());
            resume.setPersonalInfo(dto.getPersonalInfo());
            resume.setSkillsSummary(dto.getSkillsSummary());
            
            // IMPORTANT: Set userId before saving
            log.debug("Setting userId: {}", currentUserId);
            resume.setUserId(currentUserId);
            
            // Verify userId is set
            if (resume.getUserId() == null) {
                log.error("UserId is still null after setUserId()!");
                throw new RuntimeException("Failed to set user ID");
            }

            Resume created = resumeService.createResume(resume);
            log.info("Resume created successfully with id: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (Exception e) {
            log.error("Error creating resume", e);
            throw new RuntimeException("Failed to create resume: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ResponseEntity<ResumeDto> updateResume(@PathVariable Long id, @RequestBody ResumeDto dto) {
        Resume existingResume = resumeService.getResumeById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!SecurityUtils.isAdmin() && !existingResume.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn không có quyền sửa resume này");
        }

        Resume toUpdate = new Resume();
        toUpdate.setTitle(dto.getTitle());
        toUpdate.setFullName(dto.getFullName());
        toUpdate.setEmail(dto.getEmail());
        toUpdate.setPhone(dto.getPhone());
        toUpdate.setAddress(dto.getAddress());
        toUpdate.setSummary(dto.getSummary());
        toUpdate.setPhoto(dto.getPhoto());
        toUpdate.setPersonalInfo(dto.getPersonalInfo());
        toUpdate.setSkillsSummary(dto.getSkillsSummary());

        Resume updated = resumeService.updateResume(id, toUpdate);
        return ResponseEntity.ok(toDto(updated));
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

    private ResumeDto toDto(Resume r) {
        if (r == null) return null;
        ResumeDto d = new ResumeDto();
        d.setId(r.getId());
        d.setUserId(r.getUserId());
        d.setTitle(r.getTitle());
        d.setFullName(r.getFullName());
        d.setEmail(r.getEmail());
        d.setPhone(r.getPhone());
        d.setAddress(r.getAddress());
        d.setSummary(r.getSummary());
        d.setPhoto(r.getPhoto());
        d.setPersonalInfo(r.getPersonalInfo());
        d.setSkillsSummary(r.getSkillsSummary());
        d.setCreatedAt(r.getCreatedAt());
        d.setUpdatedAt(r.getUpdatedAt());
        return d;
    }
}
