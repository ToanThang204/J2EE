package com.hutech.demo.controller;

import com.hutech.demo.model.Job;
import com.hutech.demo.model.enums.JobStatus;
import com.hutech.demo.service.JobService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping
    public ResponseEntity<?> getAllJobs(@RequestParam(required = false) Integer limit) {
        try {
            List<Job> jobs = jobService.getAllJobs();
            // Chỉ lấy jobs đang OPEN
            jobs = jobs.stream()
                    .filter(job -> job.getStatus() == JobStatus.OPEN)
                    .sorted((j1, j2) -> j2.getCreatedAt().compareTo(j1.getCreatedAt()))
                    .toList();
            
            if (limit != null && limit > 0 && jobs.size() > limit) {
                jobs = jobs.subList(0, limit);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", jobs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobService.getJobById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getJobsByStatus(@PathVariable JobStatus status) {
        return ResponseEntity.ok(jobService.getJobsByStatus(status));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Job>> getJobsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(jobService.getJobsByCompany(companyId));
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<Job>> searchJobsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(jobService.searchJobsByTitle(title));
    }

    @GetMapping("/search/location")
    public ResponseEntity<List<Job>> searchJobsByLocation(@RequestParam String location) {
        return ResponseEntity.ok(jobService.searchJobsByLocation(location));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<?> createJob(@RequestBody Job job) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            job.getCompany().setUserId(currentUserId);
            if (job.getPostedDate() == null) {
                job.setPostedDate(java.time.LocalDateTime.now());
            }
            Job createdJob = jobService.createJob(job);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Đăng tin tuyển dụng thành công",
                "data", createdJob
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @RequestBody Job job) {
        try {
            Job existingJob = jobService.getJobById(id).orElseThrow(() -> new RuntimeException("Job not found"));
            
            if (!SecurityUtils.isAdmin() && !existingJob.getCompany().getUserId().equals(SecurityUtils.getCurrentUserId())) {
                throw new AccessDeniedException("Bạn không có quyền sửa job này");
            }
            
            Job updatedJob = jobService.updateJob(id, job);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật việc làm thành công",
                "data", updatedJob
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try {
            Job existingJob = jobService.getJobById(id).orElseThrow(() -> new RuntimeException("Job not found"));
            
            if (!SecurityUtils.isAdmin() && !existingJob.getCompany().getUserId().equals(SecurityUtils.getCurrentUserId())) {
                throw new AccessDeniedException("Bạn không có quyền xóa job này");
            }
            
            jobService.deleteJob(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa việc làm thành công"
            ));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
