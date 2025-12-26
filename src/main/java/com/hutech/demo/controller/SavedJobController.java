package com.hutech.demo.controller;

import com.hutech.demo.model.SavedJob;
import com.hutech.demo.service.SavedJobService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {
    private final SavedJobService savedJobService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SavedJob>> getSavedJobsByUser(@PathVariable Long userId) {
        if (!SecurityUtils.isAdmin() && !userId.equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn chỉ có thể xem saved jobs của mình");
        }
        return ResponseEntity.ok(savedJobService.getSavedJobsByUser(userId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isJobSaved(@RequestParam Long userId, @RequestParam Long jobId) {
        boolean isSaved = savedJobService.isJobSavedByUser(userId, jobId);
        return ResponseEntity.ok(isSaved);
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<SavedJob> saveJob(@RequestBody SavedJob savedJob) {
        savedJob.setUserId(SecurityUtils.getCurrentUserId());
        try {
            SavedJob createdSavedJob = savedJobService.saveJob(savedJob);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSavedJob);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> unsaveJob(@RequestParam Long userId, @RequestParam Long jobId) {
        savedJobService.unsaveJob(userId, jobId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavedJob(@PathVariable Long id) {
        savedJobService.deleteSavedJob(id);
        return ResponseEntity.noContent().build();
    }
}
